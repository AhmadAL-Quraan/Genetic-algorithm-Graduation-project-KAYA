package com.kaya.algorithm.data;

import com.kaya.model.RoomType;
import com.kaya.model.TimeSlotType; // [MODIFIED]
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

    // [MODIFIED]: Changed Map keys to TimeSlotType instead of TeachingMethod
    public static ArrayList<Lecture> excelParsing(Map<RoomType, HashSet<Room>> roomPools, Map<TimeSlotType, HashSet<TimeSlot>> timePools) {
        AtomicLong idCounter = new AtomicLong(1);
        ArrayList<Lecture> lectures = new ArrayList<>();
        Map<String, Integer> headerMap = new HashMap<>();

        // [MODIFIED]: Pre-defining the entity types to use them cleanly in the maps
        RoomType typeLecture = new RoomType("قاعة عادية");
        RoomType typeLab = new RoomType("مختبر");
        TimeSlotType typeInPerson = new TimeSlotType("وجاهي");
        TimeSlotType typeBlended = new TimeSlotType("مدمج");
        TimeSlotType typeOnline = new TimeSlotType("أونلاين");

        try (FileInputStream fis = new FileInputStream("src/main/resources/2526_first_term_sched.xlsx");
             ReadableWorkbook wb = new ReadableWorkbook(fis)) {

            Sheet sheet = wb.getFirstSheet();

            // 1. Read Header
            try (Stream<Row> headerStream = sheet.openStream()) {
                Optional<Row> headerRow = headerStream.findFirst();
                if (headerRow.isPresent()) {
                    Row header = headerRow.get();
                    for (int i = 0; i < header.getCellCount(); i++) {
                        String cellText = header.getCellText(i).trim();

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

            if (!headerMap.containsKey("START_TIME") || !headerMap.containsKey("ROOM_CODE")) {
                throw new RuntimeException("❌ ملف الإكسيل لا يحتوي على الأعمدة المطلوبة (وقت البداية أو رمز القاعة)!");
            }

            // [MODIFIED]: Initializing pools with Entity instances instead of Enums
            roomPools.put(typeLecture, new LinkedHashSet<>());
            roomPools.put(typeLab, new LinkedHashSet<>());
            timePools.put(typeBlended, new LinkedHashSet<>());
            timePools.put(typeInPerson, new LinkedHashSet<>());
            timePools.put(typeOnline, new LinkedHashSet<>());

            // 2. Read Rows
            try (Stream<Row> rows = sheet.openStream()) {
                rows.skip(1).forEach(r -> {
                    if (r.getCellCount() == 0 || r.getCellText(headerMap.getOrDefault("COURSE_SYMBOL", 0)).isEmpty()) return;

                    Course course = ExcelDataExtractor.extractCourse(r, headerMap);
                    Long classNumber = Long.parseLong(r.getCellText(headerMap.get("CLASS_NUMBER")));
                    String instructor = r.getCellText(headerMap.get("INSTRUCTOR"));
                    TimeSlot time = ExcelDataExtractor.extractTimeSlot(r, headerMap);
                    Room room  = ExcelDataExtractor.extractRoom(r, headerMap);

                    if (room == null || room.getRoomNumber().contains("Oline") || room.getRoomNumber().contains("ميدان")) {
                        return;
                    }

                    // [MODIFIED]: Using getTimeSlotType() instead of getTeachingMethod()
                    timePools.get(course.getTimeSlotType()).add(time);

                    // [MODIFIED]: Using .equals() to compare Objects instead of == which was used for Enums
                    if (course.getRequiredRoomType().equals(typeLab)){
                        room.setRoomType(typeLab);
                        roomPools.get(typeLab).add(room);
                    } else {
                        room.setRoomType(typeLecture);
                        roomPools.get(typeLecture).add(room);
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