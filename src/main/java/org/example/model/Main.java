package org.example.model;
import org.dhatim.fastexcel.reader.ReadableWorkbook;
import org.dhatim.fastexcel.reader.Row;
import org.dhatim.fastexcel.reader.Sheet;

import java.io.FileInputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;


public class Main {
    public static void main(String[] args) {
        Map<String, HashSet<Room>> roomPools = new HashMap<>(); // A dictionary that associates each room to its designated Room Group.
        Map<String, HashSet<TimeSlot>> timePools = new HashMap<>(); // A dictionary that associates each time slot to its designated TimeSlot Group.
        ArrayList<Class> classes;
        //ArrayList<ArrayList<TimeTable>> islands = new ArrayList<>();

        classes = excelParsing(roomPools, timePools); // returns an array of classes.
        int maxGenerations = 400;

        // 1. INITIALIZATION: Create the first generation
        Evolution.mutationRate = 0.15;
        Evolution.elitismCount = 2;
        Evolution.populationSize = 100;
        Evolution.tournamentSize = 5;
        ArrayList<TimeTable> islandPop = Evolution.initializePopulation(classes, timePools, roomPools);
        // 2. EVOLUTION LOOP
        islandPop = Evolution.evolveGenerations(islandPop, maxGenerations, timePools, roomPools);
        // 3. RESULTS: Print the best found schedule
        islandPop.sort((a, b) -> Integer.compare(b.fitness, a.fitness)); // sorts the TimeTables based on their fitness.
        System.out.println(islandPop.getFirst().report);
        System.out.println(islandPop.getFirst());
        // هذا الكود الي تحت اسحب عليه، حاولت انه أحسن الخوارزمية بس ما زبطتش، لبعدين بشوفه إن شاء الله
        /*
        for (int i = 0; i < 5; i++) {
            ArrayList<TimeTable> islandPop = Evolution.initializePopulation(classes, timePools, roomPools);
            // 2. EVOLUTION LOOP
            islandPop = Evolution.evolveGenerations(islandPop, maxGenerations, timePools, roomPools);
            // 3. RESULTS: Print the best found schedule
            islandPop.sort((a, b) -> Integer.compare(b.fitness, a.fitness));
            islands.add(islandPop);
        }
        maxGenerations = 300;
        ArrayList<TimeTable> finalPopulation = Evolution.islandsMerge(islands);
        Evolution.mutationRate = 0.4;

        finalPopulation =  Evolution.evolveGenerations(finalPopulation, maxGenerations, timePools, roomPools);         */
    }
    // this method simply parses the input Excel (.xlsx) file and extracts information like classes, courses, instructors, etc.
    public static ArrayList<Class> excelParsing(Map<String, HashSet<Room>> roomPools, Map<String, HashSet<TimeSlot>> timePools) {
        AtomicInteger idCounter = new AtomicInteger(1);
        ArrayList<Class> classes = new ArrayList<>();

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
                    int classNumber;
                    Course course;
                    String instructor;
                    TimeSlot time;
                    Room room;
                    Class gene;
                    int ID;

                    course = Course.extractCourse(r);
                    classNumber = Integer.parseInt(r.getCellText(2));
                    instructor = r.getCellText(19);
                    time = TimeSlot.extractTimeSlot(r, startTimeIndex, endTimeIndex);
                    room  = Room.extractRoom(r);
                    if (room.number.equals("Oline") || room.number.equals("ميدان")) // we ignore both Online and ميدان courses.
                        return ;
                    timePools.get(r.getCellText(20)).add(time);
                    // add() returns false if duplicate exists, but since it's a Set,
                    // it simply won't add it. No "if" check needed for uniqueness.
                    if (course.number.contains("L")){
                        room.groups.add ("LAB");
                        roomPools.get("LAB").add(room);
                    }
                    else {
                        room.groups.add("LECTURE");
                        roomPools.get("LECTURE").add(room);
                    }
                    ID = idCounter.getAndIncrement();
                    gene = new Class(course, classNumber, instructor, null, null, ID);
                    classes.add(gene);
                });
            } // parsing the Excel file and extracting classes
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (classes);
    }
}
