package org.example.model;

import org.dhatim.fastexcel.reader.Row;

class Course {
    public String symbol;
    public String number;
    public String[] majors;
    public String roomGroup;
    public String timeGroup;

    Course(String symbol, String number, String roomGroup, String timeGroup,String[] majors) {
        this.majors = majors;
        this.number = number;
        this.symbol = symbol;
        this.roomGroup = roomGroup;
        this.timeGroup = timeGroup;
    }

    public static Course extractCourse(Row r) {
        String course_symbol;
        String course_number;
        String teaching_system;
        String roomGroup = "";
        String timeGroup = "";

        course_symbol = r.getCellText(0);
        course_number = r.getCellText(1);
        teaching_system = r.getCellText(20);
        if (course_number.contains("L")) {
            roomGroup = "LAB";
        }
        else {
            roomGroup = "LECTURE";
        }
        timeGroup = teaching_system;
        return new Course(course_symbol, course_number, roomGroup, timeGroup, new String[1]) ;
    }

    @Override
    public String toString() {
        return symbol + " " + number;
    }

}