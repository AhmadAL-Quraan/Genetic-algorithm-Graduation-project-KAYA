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
        System.out.println(islandPop.getFirst().convertToJson());
    }
    // this method simply parses the input Excel (.xlsx) file and extracts information like classes, courses, instructors, etc.
    public static ArrayList<Class> excelParsing(Map<String, HashSet<Room>> roomPools, Map<String, HashSet<TimeSlot>> timePools) {
        AtomicInteger idCounter = new AtomicInteger(1);
        ArrayList<Class> classes = new ArrayList<>();
        Map<String, Integer> columnIndexMap = new HashMap<>();

        try (FileInputStream fis = new FileInputStream("src/main/resources/2526_first_term_sched.xlsx"); ReadableWorkbook wb = new ReadableWorkbook(fis)) {
            Sheet sheet = wb.getFirstSheet();
            // Get ONLY the header index without loading the whole file
            try (Stream<Row> headerStream = sheet.openStream()) {
                Optional<Row> headerRow = headerStream.findFirst(); // Only reads the first line
                if (headerRow.isPresent()) {
                    Row header = headerRow.get();
                    // Store each column header name and its index in key/value pairs respectively
                    for (int i = 0; i < header.getCellCount(); i++) {
                            columnIndexMap.put(header.getCellText(i).trim(), i);
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

            // If we didn't find the column, we can't continue TBD

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

                    course = Course.extractCourse(r, columnIndexMap);
                    classNumber = Integer.parseInt(r.getCellText(columnIndexMap.get("الشعبة")));
                    instructor = r.getCellText(columnIndexMap.get("المحاضر"));
                    time = TimeSlot.extractTimeSlot(r, columnIndexMap);
                    room  = Room.extractRoom(r, columnIndexMap);
                    if (room.number.equals("Oline") || room.number.equals("ميدان")) // we ignore both Online and ميدان courses.
                        return ;
                    timePools.get(r.getCellText(columnIndexMap.get("طريقة  تدريس المساق"))).add(time);
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
