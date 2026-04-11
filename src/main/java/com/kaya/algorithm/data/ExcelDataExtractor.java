package com.kaya.algorithm.data;

import com.kaya.model.Course;
import com.kaya.model.Room;
import com.kaya.model.TimeSlot;
import org.dhatim.fastexcel.reader.Row;

import java.time.LocalTime;
import java.util.*;

// الكلاس ده وظيفته الوحيدة ياخد صفوف الإكسيل ويحولها للـ Models بتاعتنا
public class ExcelDataExtractor {

    public static Room extractRoom(Row r) {
        String building;
        String number;
        // Room number extraction
        String input = r.getCellText(17); // مق 205

        // 1. trim() removes any sneaky spaces at the very beginning or end of the cell.
        // 2. split("\\s+") splits the string wherever there is one OR MORE spaces.
        String[] parts = input.trim().split("\\s+");

        // Always good to check the length just in case an Excel cell was formatted weirdly
        if (parts.length >= 2) {
            building = parts[0]; // "مق"
            number = parts[1];   // "205"
        } else {
            System.out.println("Could not split the string properly: " + input);
            return null;
        }

        return new Room(building, number, new ArrayList<String>());
    }


    // Delete
    // Delete
    // Delete
    // extracts course information from an Excel row.
    public static Course extractCourse(Row r) {
        String course_symbol;
        String course_number;
        String teaching_system;
        HashSet<String> roomGroups = new HashSet<>();
        HashSet<String> timeGroups = new HashSet<>();

        course_symbol = r.getCellText(0);
        course_number = r.getCellText(1);
        teaching_system = r.getCellText(20);
        if (course_number.contains("L")) {
            roomGroups.add("LAB");
        }
        else {
            roomGroups.add ( "LECTURE");
        }
        timeGroups.add(teaching_system);
        List<String> ls = new ArrayList<String>(1);
        return new Course(course_symbol, course_number, ls, roomGroups, timeGroups) ;
    }

    // Delete
    // Delete
    // Delete
    public static TimeSlot extractTimeSlot(Row r, int startTimeIndex, int endTimeIndex) {
        Set<String> days = new HashSet<>();
        LocalTime startTime = null;
        LocalTime endTime = null;
        ArrayList<String> timeGroup = new ArrayList<String>();

        if (r.getCellText(6).equals("X")) days.add("Saturday");
        if (r.getCellText(7).equals("X")) days.add("Sunday");
        if (r.getCellText(8).equals("X")) days.add("Monday");
        if (r.getCellText(9).equals("X")) days.add("Tuesday");
        if (r.getCellText(10).equals("X")) days.add("Wednesday");
        if (r.getCellText(11).equals("X")) days.add("Thursday");
        if (r.getCellText(12).equals("X")) days.add("Friday");

        Double timeFractionStart = r.getCellAsNumber(startTimeIndex).get().doubleValue();
        Double timeFractionEnd = r.getCellAsNumber(endTimeIndex).get().doubleValue();

        if (timeFractionStart != null && timeFractionEnd != null) {
            // Convert fraction of day to total seconds
            // 86,400 = total seconds in a day (24 * 60 * 60)
            long totalSecondsStart = Math.round(timeFractionStart * 86400);
            long totalSecondsEnd = Math.round(timeFractionEnd * 86400);
            endTime = LocalTime.ofSecondOfDay(totalSecondsEnd);
            startTime = LocalTime.ofSecondOfDay(totalSecondsStart);
        }
        else {
            System.out.println("error parsing time");
            System.exit(1);
        }
        timeGroup.add(r.getCellText(20));
        return new TimeSlot(startTime, endTime, days, timeGroup);
    }

    public static boolean conflictsWith(TimeSlot self, TimeSlot other) {
        // Collections.disjoint returns true if they have NOTHING in common.
        // So we negate it (!) to see if they share at least one day.
        if (!Collections.disjoint(self.days, other.days)) {
            return self.startTime.isBefore(other.endTime) &&
                    other.startTime.isBefore(self.endTime);
        }
        return false;
    }
}