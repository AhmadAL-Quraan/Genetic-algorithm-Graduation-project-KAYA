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
        Map<String, Set<Room>> roomPools = new HashMap<>();
        Map<String, Set<TimeSlot>> timePools = new HashMap<>();
        ArrayList<Class> classes = new ArrayList<>();


        classes = excelParsing(roomPools, timePools);

        int populationSize = 100;
        int maxGenerations = 1000;
        int tournamentSize = 5;
        double mutationRate = 0.05;
        int elitismCount = 2;

        // 1. INITIALIZATION: Create the first generation
        List<TimeTable> population = new ArrayList<>();
        for (int i = 0; i < populationSize; i++) {
            // Deep copy the initial class list so each schedule is unique
            ArrayList<Class> individualClasses = new ArrayList<>();
            for (Class c : classes) {
                individualClasses.add(new Class(c.course, c.number, c.instructor, null, null, c.ID));
            }
            TimeTable tt = new TimeTable(individualClasses);
            tt.initializeRandomly(timePools, roomPools);
            tt.calculateFitness();
            population.add(tt);
        }

        // 2. EVOLUTION LOOP
        for (int gen = 1; gen <= maxGenerations; gen++) {
            // Sort the timetables based on fitness for Elitism (Highest fitness first)
            population.sort((a, b) -> Integer.compare(b.fitness, a.fitness));
            System.out.println("Generation " + gen + " | Best Fitness: " + population.get(0).fitness);

            // Check if we found a perfect solution
            if (population.get(0).fitness == 0) {
                System.out.println("--- Perfect Schedule Found! ---");
                break;
            }

            List<TimeTable> nextGen = new ArrayList<>();

            // ELITISM: Keep the best survivors
            for (int i = 0; i < elitismCount; i++) {
                nextGen.add(population.get(i));
            }

            // REPRODUCTION: Fill up the rest of the generation
            while (nextGen.size() < populationSize) {
                TimeTable p1 = TimeTable.tournamentSelection(population, tournamentSize);
                TimeTable p2 = TimeTable.tournamentSelection(population, tournamentSize);

                TimeTable child = TimeTable.crossover(p1, p2);

                // MUTATION
                if (Math.random() < mutationRate) {
                    child.mutate(timePools, roomPools);
                }

                child.calculateFitness();
                nextGen.add(child);
            }
            population = nextGen;
        }

        // 3. RESULTS: Print the best found schedule
        population.sort((a, b) -> Integer.compare(b.fitness, a.fitness));
        System.out.println("\nFinal Result:\n" + population.get(0));
    }

    public static ArrayList<Class> excelParsing(Map<String, Set<Room>> roomPools, Map<String, Set<TimeSlot>> timePools) {
        AtomicInteger idCounter = new AtomicInteger(1);
        ArrayList<Class> classes = new ArrayList<>();

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
            roomPools.put("LECTURE", new LinkedHashSet<>());
            roomPools.put("LAB", new LinkedHashSet<>());

            timePools.put("مدمج", new LinkedHashSet<>());
            timePools.put("وجاهي", new LinkedHashSet<>());
            timePools.put("1st year", new LinkedHashSet<>());

            final int startTimeIndex = foundStartTimeIndex;
            final int endTimeIndex = foundEndTimeIndex;
            // If we didn't find the column, we can't continue
            if (foundStartTimeIndex == -1) {
                throw new RuntimeException("Could not find 'StartTime' column!");
            }

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
                    // add() returns false if duplicate exists, but since it's a Set,
                    // it simply won't add it. No "if" check needed for uniqueness.
                    if (room.number.equals("Oline") || room.number.equals("ميدان"))
                        return ;
                    timePools.get(r.getCellText(20)).add(time);
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
