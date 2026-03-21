package org.example;
import org.dhatim.fastexcel.reader.ReadableWorkbook;
import org.dhatim.fastexcel.reader.Row;
import org.dhatim.fastexcel.reader.Sheet;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static java.lang.System.exit;

class Course {
    public String symbol;
    public String number;
    public String[] majors;
    public int num_prereq;

    Course(String symbol, String number, String[] majors, int num_prereq) {
        this.majors = majors;
        this.number = number;
        this.symbol = symbol;
        if (num_prereq < 0)
            System.out.println("number of prerequisites cannot be less than zero");
        else
            this.num_prereq = num_prereq;
    }
    @Override
    public String toString() {
        return symbol + " " + number;
    }

}

class Room {
    public String building;
    public String number;

    Room(String building, String number) {
        this.building = building;
        this.number = number;
    }
    public boolean equals (Room other)
    {
        return this.building.equals(other.building) && this.number.equals(other.number);
    }
    @Override
    public String toString() {
        return building + " " + number;
    }
}

class TimeSlot {
    public LocalTime start_time;
    public LocalTime end_time;
    public ArrayList<String> days;

    TimeSlot(LocalTime start_time, LocalTime end_time, ArrayList<String> days) {
        this.start_time = start_time;
        this.end_time = end_time;
        this.days = days;
    }
    public boolean conflictsWith(TimeSlot other) {
        for (String day : days)
            if (other.days.contains(day)){
                if (this.start_time.isBefore(other.end_time) && other.start_time.isBefore(this.end_time))
                    return true;
            }
        return false;
    }
    public boolean equals(TimeSlot other)
    {
        return start_time.equals(other.start_time) && end_time.equals(other.end_time) && days.equals(other.days);
    }
    @Override
    public String toString() {
        return "Days : " + days + " Start : " + start_time + " End : " + end_time;
    }
}

class Class {
    public Course course;
    public int number;
    public String instructor;
    public TimeSlot time;
    public Room room;
    public int ID;

    Class(Course course, int number, String instructor, TimeSlot time, Room room, int ID) {
        this.course = course;
        this.number = number;
        this.instructor = instructor;
        this.time = time;
        this.room = room;
        this.ID = ID;
    }
    @Override
    public String toString() {
        return "ID : " + ID + ", Course : " + course + ", Class no : " + number + ", Instructor : " + instructor + ", Time : "  + time + ", Room : " + room.building + " " + room.number;
    }
}

class TimeTable {
    public ArrayList<Class> classes;
    public int fitness;

    TimeTable(ArrayList<Class> classes) {
        this.classes = classes;
        this.fitness = 0;
    }
    public int calculateFitness() {
        int fitness = 0;
        for (int i = 0; i < classes.size(); i++) {
            Class class1 = classes.get(i);
            for (int j = i + 1; j < classes.size(); j++) {
                Class class2 = classes.get(j);
                if (class1.time.conflictsWith(class2.time) && class1.room.equals(class2.room)) {
                    System.out.println(class1 + "xxxxxxxxxxxxx\n" + class2 + "xxxxxxxxxxxxx");
                    fitness -= 10;
                }
                if (class1.time.conflictsWith(class2.time) && class1.instructor.equals(class2.instructor)) {
                    System.out.println(class1 + "xxxxxxxxxxxxx\n" + class2 + "xxxxxxxxxxxxx");
                    fitness -= 10;
                }
            }
        }
        this.fitness = fitness;
        return fitness;
    }
    @Override
    public String toString() {
        String schedule;
        schedule = "";
        for (Class c : classes) {
            schedule += c + "\n";
        }
        return schedule;
    }
}


public class Main {
    public static void main(String[] args) {
        TimeTable timeTable;
        ArrayList<Class> classes = new ArrayList<>();
        AtomicInteger idCounter = new AtomicInteger(1);

        try (FileInputStream fis = new FileInputStream("src/main/resources/2526_first_term_sched.xlsx");
             ReadableWorkbook wb = new ReadableWorkbook(fis)) {
            Sheet sheet = wb.getFirstSheet();
            // 1. Get ONLY the header index without loading the whole file
            int foundStartTimeIndex = -1;
            int foundEndTimeIndex = -1;
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
            final int startTimeIndex = foundStartTimeIndex;
            final int endTimeIndex = foundEndTimeIndex;
            // If we didn't find the column, we can't continue
            if (foundStartTimeIndex == -1) {
                throw new RuntimeException("Could not find 'StartTime' column!");
            }

            try (Stream<Row> rows = sheet.openStream()) {
                rows.skip(1).forEach(r -> {
                    Double timeFractionStart;
                    Double timeFractionEnd;
                    int classNumber;
                    Course course;
                    String instructor;
                    TimeSlot time;
                    LocalTime startTime = null;
                    LocalTime endTime = null;
                    Room room;
                    Class gene;
                    int ID = 1;
                    ArrayList<String> days = new ArrayList<>();

                    course = new Course(r.getCellText(0), r.getCellText(1), new String[1] , 0) ;
                    classNumber = Integer.parseInt(r.getCellText(2));
                    instructor = r.getCellText(19);
                    if (r.getCellText(6).equals("X"))
                        days.add("Saturday");
                    if (r.getCellText(7).equals("X"))
                        days.add("Sunday");
                    if (r.getCellText(8).equals("X"))
                        days.add("Monday");
                    if (r.getCellText(9).equals("X"))
                        days.add("Tuesday");
                    if (r.getCellText(10).equals("X"))
                        days.add("Wednesday");
                    if (r.getCellText(11).equals("X"))
                        days.add("Thursday");
                    if (r.getCellText(12).equals("X"))
                        days.add("Friday");
                    timeFractionStart = r.getCellAsNumber(startTimeIndex).get().doubleValue();
                    timeFractionEnd = r.getCellAsNumber(endTimeIndex).get().doubleValue();
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
                    time = new TimeSlot(startTime, endTime, days);
                    //Room number extraction
                    String input = r.getCellText(17); // مق 205
                    // Remove all whitespace first
                    String cleanInput = input.replaceAll("\\s+", ""); // Results in "مق205"
                    // Then split at the digit/letter boundary
                    String[] parts = cleanInput.split("(?<=\\d)(?=\\D)|(?<=\\D)(?=\\d)");
                    String building = parts[0]; // "مق"
                    String relative_number = parts[1]; // "205"
                    room  = new Room(building, relative_number);
                    if (!relative_number.equals("Oline") && !relative_number.equals("ميدان")) {
                        ID = idCounter.getAndIncrement();;
                    }
                    gene = new Class(course, classNumber, instructor, time, room, ID);
                    if (!gene.room.number.equals("Oline") && !gene.room.number.equals("ميدان")) {
                        classes.add(gene);
                    }
                });

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        timeTable = new TimeTable(classes);
        System.out.println(timeTable);
        timeTable.calculateFitness();
        System.out.println(timeTable.fitness);
    }
}