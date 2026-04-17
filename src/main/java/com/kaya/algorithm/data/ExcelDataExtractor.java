package com.kaya.algorithm.data;

import com.kaya.model.Course;
import com.kaya.model.Room;
import com.kaya.model.TimeSlot;
import com.kaya.model.enums.RoomType;
import com.kaya.model.enums.TeachingMethod;
import org.dhatim.fastexcel.reader.Row;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;

// الكلاس ده وظيفته الوحيدة ياخد صفوف الإكسيل ويحولها للـ Models بتاعتنا بناءً على خريطة الأعمدة (headerMap)
public class ExcelDataExtractor {

    // استخراج القاعة باستخدام الـ Header Map
    public static Room extractRoom(Row r, Map<String, Integer> headerMap) {
        String building;
        String number;

        // بنستخدم الـ Key عشان نجيب الـ Index ديناميكياً بدل ما نثبت رقم 17
        String input = r.getCellText(headerMap.get("ROOM_CODE"));

        if (input == null || input.trim().isEmpty()) return null;

        // 1. trim() removes any sneaky spaces at the very beginning or end of the cell.
        // 2. split("\\s+") splits the string wherever there is one OR MORE spaces.
        String[] parts = input.trim().split("\\s+");

        // Always good to check the length just in case an Excel cell was formatted weirdly
        if (parts.length >= 2) {
            building = parts[0]; // "مق"
            number = parts[1];   // "205"
        } else {
            building = input.trim();
            number = "N/A"; // في حالة القاعة ملهاش رقم
        }

        // نوع القاعة الدقيق هيتحدد في الـ Parser، فهنا بنحط قيمة مبدئية LECTURE
        return new Room(building, number, RoomType.LECTURE);
    }

    // استخراج بيانات الكورس باستخدام الـ Header Map
    public static Course extractCourse(Row r, Map<String, Integer> headerMap) {
        String course_symbol = r.getCellText(headerMap.get("COURSE_SYMBOL"));
        String course_number = r.getCellText(headerMap.get("COURSE_NUMBER"));
        String teaching_system = r.getCellText(headerMap.get("TEACHING_METHOD"));

        RoomType roomType;
        TeachingMethod teachingMethod;

        // تحديد نوع القاعة المطلوبة بناءً على حرف الـ L
        if (course_number != null && course_number.contains("L")) {
            roomType = RoomType.LAB;
        } else {
            roomType = RoomType.LECTURE;
        }

        // تحويل النص العربي لـ Enum
        if (teaching_system != null && teaching_system.contains("وجاهي")) {
            teachingMethod = TeachingMethod.IN_PERSON;
        } else if (teaching_system != null && teaching_system.contains("مدمج")) {
            teachingMethod = TeachingMethod.BLENDED;
        } else {
            teachingMethod = TeachingMethod.ONLINE;
        }

        List<String> majors = new ArrayList<>(); // مجهزة للمستقبل
        return new Course(course_symbol, course_number, majors, roomType, teachingMethod);
    }

    // استخراج الوقت والأيام باستخدام الـ Header Map
    public static TimeSlot extractTimeSlot(Row r, Map<String, Integer> headerMap) {
        Set<DayOfWeek> days = new HashSet<>();
        LocalTime startTime = null;
        LocalTime endTime = null;

        // استخراج الأيام وتحويلها لـ Java DayOfWeek Enum (أنظف وأسرع)
        if (checkDay(r, headerMap, "DAY_SAT")) days.add(DayOfWeek.SATURDAY);
        if (checkDay(r, headerMap, "DAY_SUN")) days.add(DayOfWeek.SUNDAY);
        if (checkDay(r, headerMap, "DAY_MON")) days.add(DayOfWeek.MONDAY);
        if (checkDay(r, headerMap, "DAY_TUE")) days.add(DayOfWeek.TUESDAY);
        if (checkDay(r, headerMap, "DAY_WED")) days.add(DayOfWeek.WEDNESDAY);
        if (checkDay(r, headerMap, "DAY_THU")) days.add(DayOfWeek.THURSDAY);

        // [تم الحل]: تحويل BigDecimal إلى Double بأمان تام
        Double timeFractionStart = r.getCellAsNumber(headerMap.get("START_TIME"))
                .map(java.math.BigDecimal::doubleValue)
                .orElse(null);

        Double timeFractionEnd = r.getCellAsNumber(headerMap.get("END_TIME"))
                .map(java.math.BigDecimal::doubleValue)
                .orElse(null);

        if (timeFractionStart != null && timeFractionEnd != null) {
            // Convert fraction of day to total seconds
            // 86,400 = total seconds in a day (24 * 60 * 60)
            long totalSecondsStart = Math.round(timeFractionStart * 86400);
            long totalSecondsEnd = Math.round(timeFractionEnd * 86400);
            startTime = LocalTime.ofSecondOfDay(totalSecondsStart);
            endTime = LocalTime.ofSecondOfDay(totalSecondsEnd);
        } else {
            System.err.println("❌ خطأ في قراءة الوقت للمساق في هذا الصف");
        }

        // معرفة طريقة التدريس عشان نربطها بالوقت
        String methodStr = r.getCellText(headerMap.get("TEACHING_METHOD"));
        TeachingMethod method = (methodStr != null && methodStr.contains("وجاهي")) ? TeachingMethod.IN_PERSON :
                (methodStr != null && methodStr.contains("مدمج")) ? TeachingMethod.BLENDED : TeachingMethod.ONLINE;

        return new TimeSlot(startTime, endTime, days, method);
    }

    // دالة مساعدة للتحقق من وجود علامة X في عمود اليوم
    private static boolean checkDay(Row r, Map<String, Integer> headerMap, String dayKey) {
        if (!headerMap.containsKey(dayKey)) return false;
        String cellText = r.getCellText(headerMap.get(dayKey));
        return cellText != null && cellText.trim().equalsIgnoreCase("X");
    }

    // التحقق من التعارض (بقينا نستخدم getDays و getStartTime عشان المتغيرات بقت private)
    public static boolean conflictsWith(TimeSlot self, TimeSlot other) {
        // Collections.disjoint بترجع True لو مفيش أيام مشتركة
        // فبنعكسها (!) عشان لو في أيام مشتركة، نفحص تقاطع الأوقات
        if (!Collections.disjoint(self.getDays(), other.getDays())) {
            return self.getStartTime().isBefore(other.getEndTime()) &&
                    other.getStartTime().isBefore(self.getEndTime());
        }
        return false;
    }
}