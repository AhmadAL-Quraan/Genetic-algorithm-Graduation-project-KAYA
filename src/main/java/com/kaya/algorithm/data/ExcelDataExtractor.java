package com.kaya.algorithm.data;

import com.kaya.model.Course;
import com.kaya.model.Room;
import com.kaya.model.TimeSlot;
import com.kaya.model.RoomType; // [MODIFIED]: Now an Entity, not an Enum
import com.kaya.model.TimeSlotType; // [MODIFIED]: Replaced TeachingMethod
import org.dhatim.fastexcel.reader.Row;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;

// This class parses Excel rows into our application Models based on the headerMap
public class ExcelDataExtractor {

    // Extract Room using Header Map
    public static Room extractRoom(Row r, Map<String, Integer> headerMap) {
        String building;
        String number;

        String input = r.getCellText(headerMap.get("ROOM_CODE"));

        if (input == null || input.trim().isEmpty()) return null;

        String[] parts = input.trim().split("\\s+");

        if (parts.length >= 2) {
            building = parts[0];
            number = parts[1];
        } else {
            building = input.trim();
            number = "N/A";
        }

        // [MODIFIED]: Creating a new RoomType object instead of using Enum
        return new Room(building, number, new RoomType("قاعة عادية"));
    }

    // Extract Course using Header Map
    public static Course extractCourse(Row r, Map<String, Integer> headerMap) {
        String course_symbol = r.getCellText(headerMap.get("COURSE_SYMBOL"));
        String course_number = r.getCellText(headerMap.get("COURSE_NUMBER"));
        String teaching_system = r.getCellText(headerMap.get("TEACHING_METHOD"));

        RoomType roomType;
        TimeSlotType timeSlotType; // [MODIFIED]

        // Determine required RoomType based on 'L'
        if (course_number != null && course_number.contains("L")) {
            roomType = new RoomType("مختبر");
        } else {
            roomType = new RoomType("قاعة عادية");
        }

        // [MODIFIED]: Determine TimeSlotType (previously TeachingMethod)
        if (teaching_system != null && teaching_system.contains("وجاهي")) {
            timeSlotType = new TimeSlotType("وجاهي");
        } else if (teaching_system != null && teaching_system.contains("مدمج")) {
            timeSlotType = new TimeSlotType("مدمج");
        } else {
            timeSlotType = new TimeSlotType("أونلاين");
        }

        List<String> majors = new ArrayList<>(); // Prepared for future
        return new Course(course_symbol, course_number, majors, roomType, timeSlotType);
    }

    // Extract TimeSlot and Days using Header Map
    public static TimeSlot extractTimeSlot(Row r, Map<String, Integer> headerMap) {
        Set<DayOfWeek> days = new HashSet<>();
        LocalTime startTime = null;
        LocalTime endTime = null;

        if (checkDay(r, headerMap, "DAY_SAT")) days.add(DayOfWeek.SATURDAY);
        if (checkDay(r, headerMap, "DAY_SUN")) days.add(DayOfWeek.SUNDAY);
        if (checkDay(r, headerMap, "DAY_MON")) days.add(DayOfWeek.MONDAY);
        if (checkDay(r, headerMap, "DAY_TUE")) days.add(DayOfWeek.TUESDAY);
        if (checkDay(r, headerMap, "DAY_WED")) days.add(DayOfWeek.WEDNESDAY);
        if (checkDay(r, headerMap, "DAY_THU")) days.add(DayOfWeek.THURSDAY);

        Double timeFractionStart = r.getCellAsNumber(headerMap.get("START_TIME"))
                .map(java.math.BigDecimal::doubleValue)
                .orElse(null);

        Double timeFractionEnd = r.getCellAsNumber(headerMap.get("END_TIME"))
                .map(java.math.BigDecimal::doubleValue)
                .orElse(null);

        if (timeFractionStart != null && timeFractionEnd != null) {
            long totalSecondsStart = Math.round(timeFractionStart * 86400);
            long totalSecondsEnd = Math.round(timeFractionEnd * 86400);
            startTime = LocalTime.ofSecondOfDay(totalSecondsStart);
            endTime = LocalTime.ofSecondOfDay(totalSecondsEnd);
        } else {
            System.err.println("❌ خطأ في قراءة الوقت للمساق في هذا الصف");
        }

        // [MODIFIED]: Fetching TimeSlotType instead of TeachingMethod
        String methodStr = r.getCellText(headerMap.get("TEACHING_METHOD"));
        TimeSlotType timeSlotType = (methodStr != null && methodStr.contains("وجاهي")) ? new TimeSlotType("وجاهي") :
                (methodStr != null && methodStr.contains("مدمج")) ? new TimeSlotType("مدمج") : new TimeSlotType("أونلاين");

        return new TimeSlot(startTime, endTime, days, timeSlotType);
    }

    private static boolean checkDay(Row r, Map<String, Integer> headerMap, String dayKey) {
        if (!headerMap.containsKey(dayKey)) return false;
        String cellText = r.getCellText(headerMap.get(dayKey));
        return cellText != null && cellText.trim().equalsIgnoreCase("X");
    }

    public static boolean conflictsWith(TimeSlot self, TimeSlot other) {
        if (!Collections.disjoint(self.getDays(), other.getDays())) {
            return self.getStartTime().isBefore(other.getEndTime()) &&
                    other.getStartTime().isBefore(self.getEndTime());
        }
        return false;
    }
}