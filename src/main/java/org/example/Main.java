package org.example;
import org.dhatim.fastexcel.reader.ReadableWorkbook;
import org.dhatim.fastexcel.reader.Row;
import org.dhatim.fastexcel.reader.Sheet;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

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
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Room room = (Room) o;
        return Objects.equals(building, room.building) && Objects.equals(number, room.number);
    }

    @Override
    public int hashCode() {
        return Objects.hash(building, number);
    }
    @Override
    public String toString() {
        return building + " " + number;
    }
}

class TimeSlot {
    public LocalTime start_time;
    public LocalTime end_time;
    public Set<String> days; // Changed from ArrayList to Set

    TimeSlot(LocalTime start_time, LocalTime end_time, Set<String> days) {
        this.start_time = start_time;
        this.end_time = end_time;
        this.days = days;
    }

    public boolean conflictsWith(TimeSlot other) {
        // Collections.disjoint returns true if they have NOTHING in common.
        // So we negate it (!) to see if they share at least one day.
        if (!Collections.disjoint(this.days, other.days)) {
            return this.start_time.isBefore(other.end_time) &&
                    other.start_time.isBefore(this.end_time);
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeSlot timeSlot = (TimeSlot) o;
        return Objects.equals(start_time, timeSlot.start_time) &&
                Objects.equals(end_time, timeSlot.end_time) &&
                Objects.equals(days, timeSlot.days); // Set equality ignores order!
    }

    @Override
    public int hashCode() {
        return Objects.hash(start_time, end_time, days);
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
        return "ID : " + ID + ", Course : " + course + ", Class no : " + number + ", Instructor : " + instructor + ", Time : "  + time + ", Room : " + room;
    }
}

class TimeTable {
    public ArrayList<Class> classes;
    public int fitness;

    TimeTable(ArrayList<Class> classes) {
        this.classes = classes;
        this.fitness = 0;
    }
    public void mutate(List<TimeSlot> timePool, List<Room> roomPool) {
        Random rand = new Random();
        // Pick ONE random class to change
        Class randomClass = this.classes.get(rand.nextInt(this.classes.size()));

        if (rand.nextBoolean()) {
            // 50% chance to change the Room
            randomClass.room = roomPool.get(rand.nextInt(roomPool.size()));
        } else {
            // 50% chance to change the TimeSlot
            randomClass.time = timePool.get(rand.nextInt(timePool.size()));
        }
    }

    // Static Crossover: Creates a child from two parents
    public static TimeTable crossover(TimeTable p1, TimeTable p2) {
        Random rand = new Random();
        int split = rand.nextInt(p1.classes.size());
        ArrayList<Class> childClasses = new ArrayList<>();

        for (int i = 0; i < p1.classes.size(); i++) {
            Class source = (i < split) ? p1.classes.get(i) : p2.classes.get(i);
            // Create a NEW Class object but keep the same Course/Instructor
            // We take the Room and TimeSlot from the chosen parent
            childClasses.add(new Class(source.course, source.number, source.instructor, source.time, source.room, source.ID));
        }
        return new TimeTable(childClasses);
    }

    // Tournament Selection: Picks the best K random individuals out of the timeTables population
    public static TimeTable tournamentSelection(List<TimeTable> population, int k) {
        Random rand = new Random();
        TimeTable best = null;
        for (int i = 0; i < k; i++) {
            TimeTable ind = population.get(rand.nextInt(population.size()));
            if (best == null || ind.fitness > best.fitness) {
                best = ind;
            }
        }
        return best;
    }

    public void initializeRandomly(List<TimeSlot> timePool, List<Room> roomPool) {
        Random rand = new Random();
        for (Class c : this.classes) {
            // Randomly pick an allele from the pools
            c.time = timePool.get(rand.nextInt(timePool.size()));
            c.room = roomPool.get(rand.nextInt(roomPool.size()));
        }
    }

    public int calculateFitness() {
        int totalFitness = 0;

        // 1. Group classes by Room and by Instructor
        Map<Room, List<Class>> roomGroups = new HashMap<>();
        Map<String, List<Class>> instructorGroups = new HashMap<>();

        for (Class c : classes) {
            roomGroups.computeIfAbsent(c.room, k -> new ArrayList<>()).add(c);
            instructorGroups.computeIfAbsent(c.instructor, k -> new ArrayList<>()).add(c);
        }

        // 2. Check conflicts only within the same Room
        for (List<Class> roomList : roomGroups.values()) {
            totalFitness += checkInternalConflicts(roomList, "Room Conflict");
        }

        // 3. Check conflicts only within the same Instructor
        for (List<Class> instructorList : instructorGroups.values()) {
            totalFitness += checkInternalConflicts(instructorList, "Instructor Conflict");
        }

        this.fitness = totalFitness;
        return totalFitness;
    }

    private int checkInternalConflicts(List<Class> group, String conflictType) {
        int penalty = 0;
        // Only compare items within this specific small group
        for (int i = 0; i < group.size(); i++) {
            for (int j = i + 1; j < group.size(); j++) {
                Class c1 = group.get(i);
                Class c2 = group.get(j);

                if (c1.time.conflictsWith(c2.time)) {
                    //System.out.println(conflictType + " between: " + c1.ID + " and " + c2.ID);
                    penalty -= 10;
                }
            }
        }
        return penalty;
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
        Set<TimeSlot> timeSlots = new LinkedHashSet<>();
        Set<Room> rooms = new LinkedHashSet<>();
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
                    int ID = -1;
                    Set<String> days = new HashSet<>();

                    course = new Course(r.getCellText(0), r.getCellText(1), new String[1] , 0) ;
                    classNumber = Integer.parseInt(r.getCellText(2));
                    instructor = r.getCellText(19);
                    if (r.getCellText(6).equals("X")) days.add("Saturday");
                    if (r.getCellText(7).equals("X")) days.add("Sunday");
                    if (r.getCellText(8).equals("X")) days.add("Monday");
                    if (r.getCellText(9).equals("X")) days.add("Tuesday");
                    if (r.getCellText(10).equals("X")) days.add("Wednesday");
                    if (r.getCellText(11).equals("X")) days.add("Thursday");
                    if (r.getCellText(12).equals("X")) days.add("Friday");

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
                    // add() returns false if duplicate exists, but since it's a Set,
                    // it simply won't add it. No "if" check needed for uniqueness.
                    if (relative_number.equals("Oline") || relative_number.equals("ميدان"))
                        return ;
                    timeSlots.add(time);
                    rooms.add(room);
                    ID = idCounter.getAndIncrement();
                    gene = new Class(course, classNumber, instructor, null, null, ID);
                    classes.add(gene);
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<TimeSlot> timePool = new ArrayList<>(timeSlots);
        List<Room> roomPool = new ArrayList<>(rooms);

        int populationSize = 100;
        int maxGenerations = 500;
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
            tt.initializeRandomly(timePool, roomPool);
            tt.calculateFitness();
            population.add(tt);
        }

        // 2. EVOLUTION LOOP
        for (int gen = 1; gen <= maxGenerations; gen++) {
            // Sort for Elitism (Highest fitness first)
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
                    child.mutate(timePool, roomPool);
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
}
