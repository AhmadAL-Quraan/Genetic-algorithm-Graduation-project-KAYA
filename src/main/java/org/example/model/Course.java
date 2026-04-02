package org.example.model;

import org.dhatim.fastexcel.reader.Row;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class Course {
    public String symbol;
    public String number;
    public String[] majors;
    public HashSet<String> roomGroups;
    public HashSet<String> timeGroups;

    Course(String symbol, String number, HashSet<String> roomGroups, HashSet<String> timeGroups,String[] majors) {
        this.majors = majors;
        this.number = number;
        this.symbol = symbol;
        this.roomGroups = roomGroups;
        this.timeGroups = timeGroups;
    }
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
        return new Course(course_symbol, course_number, roomGroups, timeGroups, new String[1]) ;
    }



    @Override
    public String toString() {
        return symbol + " " + number;
    }

}