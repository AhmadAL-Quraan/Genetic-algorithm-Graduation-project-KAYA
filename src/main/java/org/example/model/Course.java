package org.example.model;

import org.dhatim.fastexcel.reader.Row;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class Course {
    public String symbol;
    public String number;
    public HashSet<String> roomGroups;
    public HashSet<String> timeGroups;

    Course(String symbol, String number, HashSet<String> roomGroups, HashSet<String> timeGroups) {
        this.number = number;
        this.symbol = symbol;
        this.roomGroups = roomGroups;
        this.timeGroups = timeGroups;
    }
    // extracts course information from an Excel row.
    public static Course extractCourse(Row r, Map<String, Integer> columnIndexMap) {
        String course_symbol;
        String course_number;
        String teaching_system;
        HashSet<String> roomGroups = new HashSet<>();
        HashSet<String> timeGroups = new HashSet<>();

        course_symbol = r.getCellText(columnIndexMap.get("رمز المساق"));
        course_number = r.getCellText(columnIndexMap.get("رقم المساق"));
        teaching_system = r.getCellText(columnIndexMap.get("طريقة  تدريس المساق"));
        if (course_number.contains("L")) {
            roomGroups.add("LAB");
        }
        else {
            roomGroups.add ( "LECTURE");
        }
        timeGroups.add(teaching_system);
        return new Course(course_symbol, course_number, roomGroups, timeGroups) ;
    }



    @Override
    public String toString() {
        return symbol + " " + number;
    }

}