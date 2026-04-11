package com.kaya.algorithm.data;

import com.kaya.model.enums.RoomType;
import com.kaya.model.enums.TeachingMethod;
import org.dhatim.fastexcel.reader.ReadableWorkbook;
import org.dhatim.fastexcel.reader.Row;
import org.dhatim.fastexcel.reader.Sheet;
import com.kaya.model.Course;
import com.kaya.model.Lecture;
import com.kaya.model.Room;
import com.kaya.model.TimeSlot;

import java.io.FileInputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

public class ExcelParser {

    // [تعديل هام]: غيرنا الـ String لـ Enums في الـ Maps عشان النضافة والسرعة
    public static ArrayList<Lecture> excelParsing(Map<RoomType, HashSet<Room>> roomPools, Map<TeachingMethod, HashSet<TimeSlot>> timePools) {
        AtomicLong idCounter = new AtomicLong(1);
        ArrayList<Lecture> lectures = new ArrayList<>();

        // هنا هنخزن اسم العمود (Key) ورقم الـ Index بتاعه (Value)
        Map<String, Integer> headerMap = new HashMap<>();

        try (FileInputStream fis = new FileInputStream("src/main/resources/2526_first_term_sched.xlsx");
             ReadableWorkbook wb = new ReadableWorkbook(fis)) {

            Sheet sheet = wb.getFirstSheet();

            // 1. قراءة الـ Header فقط لمعرفة أماكن الأعمدة
            try (Stream<Row> headerStream = sheet.openStream()) {
                Optional<Row> headerRow = headerStream.findFirst();
                if (headerRow.isPresent()) {
                    Row header = headerRow.get();
                    for (int i = 0; i < header.getCellCount(); i++) {
                        String cellText = header.getCellText(i).trim();

                        // بنربط الكلمات اللي في الإكسيل بالمفاتيح الثابتة بتاعتنا
                        if (cellText.contains("رمز المساق")) headerMap.put("COURSE_SYMBOL", i);
                        else if (cellText.contains("رقم المساق")) headerMap.put("COURSE_NUMBER", i);
                        else if (cellText.contains("الشعبة") && !cellText.contains("ملغاة")) headerMap.put("CLASS_NUMBER", i);
                        else if (cellText.contains("وقت البداية")) headerMap.put("START_TIME", i);
                        else if (cellText.contains("وقت النهاية")) headerMap.put("END_TIME", i);
                        else if (cellText.contains("رمز القاعة")) headerMap.put("ROOM_CODE", i);
                        else if (cellText.contains("المحاضر")) headerMap.put("INSTRUCTOR", i);
                        else if (cellText.contains("طريقة") && cellText.contains("تدريس")) headerMap.put("TEACHING_METHOD", i);
                        else if (cellText.equals("سبت")) headerMap.put("DAY_SAT", i);
                        else if (cellText.equals("حد")) headerMap.put("DAY_SUN", i);
                        else if (cellText.equals("ثن")) headerMap.put("DAY_MON", i);
                        else if (cellText.equals("ثل")) headerMap.put("DAY_TUE", i);
                        else if (cellText.equals("ربع")) headerMap.put("DAY_WED", i);
                        else if (cellText.equals("خمس")) headerMap.put("DAY_THU", i);
                    }
                }
            }

            // التأكد من إن العواميد الأساسية موجودة (خطوة أمان)
            if (!headerMap.containsKey("START_TIME") || !headerMap.containsKey("ROOM_CODE")) {
                throw new RuntimeException("❌ ملف الإكسيل لا يحتوي على الأعمدة المطلوبة (وقت البداية أو رمز القاعة)!");
            }

            // تهيئة الـ Pools باستخدام الـ Enums
            roomPools.put(RoomType.LECTURE, new LinkedHashSet<>());
            roomPools.put(RoomType.LAB, new LinkedHashSet<>());
            timePools.put(TeachingMethod.BLENDED, new LinkedHashSet<>());
            timePools.put(TeachingMethod.IN_PERSON, new LinkedHashSet<>());
            timePools.put(TeachingMethod.ONLINE, new LinkedHashSet<>());

            // 2. قراءة البيانات صفا صفا باستخدام الـ headerMap
            try (Stream<Row> rows = sheet.openStream()) {
                rows.skip(1).forEach(r -> {
                    // لو الصف فاضي نعمله Skip
                    if (r.getCellCount() == 0 || r.getCellText(headerMap.getOrDefault("COURSE_SYMBOL", 0)).isEmpty()) return;

                    // استخراج البيانات من الكلاس المساعد وبنبعتله الخريطة (headerMap)
                    Course course = ExcelDataExtractor.extractCourse(r, headerMap);
                    Long classNumber = Long.parseLong(r.getCellText(headerMap.get("CLASS_NUMBER")));
                    String instructor = r.getCellText(headerMap.get("INSTRUCTOR"));
                    TimeSlot time = ExcelDataExtractor.extractTimeSlot(r, headerMap);
                    Room room  = ExcelDataExtractor.extractRoom(r, headerMap);

                    // استثناء الأونلاين والميدان
                    if (room == null || room.getRoomNumber().contains("Oline") || room.getRoomNumber().contains("ميدان")) {
                        return;
                    }

                    // إضافة الوقت للـ timePools المناسب
                    timePools.get(course.getTeachingMethod()).add(time);

                    // إضافة القاعة للـ roomPools المناسب وتحديد نوعها
                    if (course.getRequiredRoomType() == RoomType.LAB){
                        room.setGroups(RoomType.LAB);
                        roomPools.get(RoomType.LAB).add(room);
                    } else {
                        room.setGroups(RoomType.LECTURE);
                        roomPools.get(RoomType.LECTURE).add(room);
                    }

                    Long ID = idCounter.getAndIncrement();
                    Lecture gene = new Lecture(ID, course, room, time, classNumber, instructor);
                    lectures.add(gene);
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lectures;
    }
}