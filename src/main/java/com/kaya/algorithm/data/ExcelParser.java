package com.kaya.algorithm.data;

import org.dhatim.fastexcel.reader.ReadableWorkbook;
import org.dhatim.fastexcel.reader.Row;
import org.dhatim.fastexcel.reader.Sheet;
import com.kaya.model.Course;
import com.kaya.model.Lecture;
import com.kaya.model.Room;
import com.kaya.model.TimeSlot;

import java.io.FileInputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

public class ExcelParser {

    // this method simply parses the input Excel (.xlsx) file and extracts information like classes, courses, instructors, etc.
    public static ArrayList<Lecture> excelParsing(Map<String, HashSet<Room>> roomPools, Map<String, HashSet<TimeSlot>> timePools) {
        AtomicLong idCounter = new AtomicLong(1);
        ArrayList<Lecture> lectures = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream("src/main/resources/2526_first_term_sched.xlsx");
             ReadableWorkbook wb = new ReadableWorkbook(fis)) {
            Sheet sheet = wb.getFirstSheet();
            // 1. Get ONLY the header index without loading the whole file
            int foundStartTimeIndex = -1;
            int foundEndTimeIndex = -1;
            // This try statement tries to find the index of وقت البداية column and وقت النهاية column.
            try (Stream<Row> headerStream = sheet.openStream()) {
                Optional<Row> headerRow = headerStream.findFirst(); // Only reads the first line
                if (headerRow.isPresent()) {
                    Row header = headerRow.get();
                    for (int i = 0; i < header.getCellCount(); i++) {
                        if (header.getCellText(i).contains("وقت البداية")) {
                            foundStartTimeIndex = i;
                        }
                        if (header.getCellText(i).contains("وقت النهاية")) {
                            foundEndTimeIndex = i;
                        }
                    }
                }
            }
            // We categorize rooms to either a LECTURE or a LAB to put courses like (CS 111L) in LAB rooms and other
            // courses in LECTURE rooms.
            roomPools.put("LECTURE", new LinkedHashSet<>());
            roomPools.put("LAB", new LinkedHashSet<>());
            // We categorize time slots to either مدمج or وجاهي to put each course in its suitable time.
            timePools.put("مدمج", new LinkedHashSet<>());
            timePools.put("وجاهي", new LinkedHashSet<>());

            final int startTimeIndex = foundStartTimeIndex;
            final int endTimeIndex = foundEndTimeIndex;
            // If we didn't find the column, we can't continue
            if (foundStartTimeIndex == -1) {
                throw new RuntimeException("Could not find 'StartTime' column!");
            }
            // here we go through all Excel rows
            try (Stream<Row> rows = sheet.openStream()) {
                rows.skip(1).forEach(r -> {
                    Long classNumber;
                    Course course;
                    String instructor;
                    TimeSlot time;
                    Room room;
                    Lecture gene;
                    Long ID;

                    course = ExcelDataExtractor.extractCourse(r);
                    classNumber = Long.parseLong(r.getCellText(2));
                    instructor = r.getCellText(19);
                    time = ExcelDataExtractor.extractTimeSlot(r, startTimeIndex, endTimeIndex);
                    room  = ExcelDataExtractor.extractRoom(r);
                    if (room.getRoomNumber().equals("Oline") || room.getRoomNumber().equals("ميدان")) // we ignore both Online and ميدان courses.
                        return ;
                    timePools.get(r.getCellText(20)).add(time);
                    // add() returns false if duplicate exists, but since it's a Set,
                    // it simply won't add it. No "if" check needed for uniqueness.
                    if (course.getCourseNumber().contains("L")){
                        room.getGroups().add ("LAB");
                        roomPools.get("LAB").add(room);
                    }
                    else {
                        room.getGroups().add("LECTURE");
                        roomPools.get("LECTURE").add(room);
                    }
                    ID = idCounter.getAndIncrement();
                    gene = new Lecture(ID, course, null, null, classNumber, instructor);
                    lectures.add(gene);
                });
            } // parsing the Excel file and extracting classes
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (lectures);
    }
}