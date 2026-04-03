package org.example.model;

import org.example.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.*;

class TimeTable {
    public ArrayList<Class> classes;
    public int fitness;
    public FitnessReport report;

    TimeTable(ArrayList<Class> classes) {
        this.classes = classes;
        this.fitness = 0;
        this.report = new FitnessReport();
    }

    public void mutate(Map<String, HashSet<TimeSlot>> timePools, Map<String, HashSet<Room>> roomPools) {
        List<TimeSlot> finalTimePool;
        List<Room> finalRoomPool;
        ArrayList<Class> classPool;

        Random rand = new Random();
        // Pick ONE random class to change
        classPool = new ArrayList<>(report.conflictingClasses); // report.conflictingClasses contains all classes that
        // face conflicts with other conflicts so that mutations don't change an already non-conflicting class. ذكاء مش صح ؟
        Class randomClass = classPool.get(rand.nextInt(classPool.size()));
        finalTimePool = new ArrayList<>(randomClass.getTimeSlots(timePools));
        finalRoomPool = new ArrayList<>(randomClass.getRooms(roomPools));
        if (rand.nextBoolean()) {
            // 50% chance to change the Room
            randomClass.room = finalRoomPool.get(rand.nextInt(finalRoomPool.size()));
        } else {
            // 50% chance to change the TimeSlot
            randomClass.time = finalTimePool.get(rand.nextInt(finalTimePool.size()));
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



    public void initializeRandomly(Map<String, HashSet<TimeSlot>> timePools, Map<String, HashSet<Room>> roomPools) {
        List<TimeSlot> timePool;
        List<Room> roomPool;


        Random rand = new Random();
        for (Class c : this.classes) {
            timePool = new ArrayList<>(c.getTimeSlots(timePools));
            roomPool = new ArrayList<>(c.getRooms(roomPools));
            // Randomly pick an allele(or a gene) from the pools
            c.time = timePool.get(rand.nextInt(timePool.size()));
            c.room = roomPool.get(rand.nextInt(roomPool.size()));
        }
        this.calculateFitness();
    }

    public int calculateFitness() {
        int totalFitness = 0;
        report.totalPenalty = 0;
        report.studentConflicts = 0;
        report.instructorConflicts = 0;
        report.roomConflicts = 0;

        Map<Room, List<Class>> roomGroups = new HashMap<>();
        Map<String, List<Class>> instructorGroups = new HashMap<>();
        // NEW: Grouping by Department + Year Level
        Map<String, List<Class>> deptYearGroups = new HashMap<>();

        for (Class c : classes) {
            roomGroups.computeIfAbsent(c.room, k -> new ArrayList<>()).add(c);
            instructorGroups.computeIfAbsent(c.instructor, k -> new ArrayList<>()).add(c);

            // Create a unique key like "BIT-2"
            String deptYearKey = c.course.symbol + "-" + c.course.number.charAt(0);
            deptYearGroups.computeIfAbsent(deptYearKey, k -> new ArrayList<>()).add(c);
        }

        // 1. Hard Conflicts (Room)
        for (List<Class> roomList : roomGroups.values()) {
            totalFitness += checkInternalConflicts(roomList, "Room Conflict", 10);
        }

        // 2. Hard Conflicts (Instructor)
        for (List<Class> instructorList : instructorGroups.values()) {
            totalFitness += checkInternalConflicts(instructorList, "Instructor Conflict", 10);
        }

        // 3. Soft Conflicts (Same Year/Dept Students)
        for (List<Class> deptYearList : deptYearGroups.values()) {
            // Use a smaller penalty (e.g., -20) since this is a soft constraint
            totalFitness += checkInternalConflicts(deptYearList, "Student Year Conflict", 5);
        }
        this.fitness = totalFitness;
        return totalFitness;
    }

    private int checkInternalConflicts(List<Class> group, String conflictType, int penalty_weight) {
        int penalty = 0;
        // Only compare items within this specific small group
        for (int i = 0; i < group.size(); i++) {
            for (int j = i + 1; j < group.size(); j++) {
                Class c1 = group.get(i);
                Class c2 = group.get(j);

                if (c1.time.conflictsWith(c2.time)) {
                    report.conflictingClasses.add(c1);
                    report.conflictingClasses.add(c2);
                    penalty -= penalty_weight;
                    report.totalPenalty -= penalty_weight;
                    if (conflictType.equals("Room Conflict")) {
                        report.roomConflicts+=1;
                    }
                    else if(conflictType.equals("Instructor Conflict")) {
                        report.instructorConflicts+=1;
                    }
                    else if(conflictType.equals("Student Year Conflict")) {
                        report.studentConflicts+=1;
                    }
                }
            }
        }
        return penalty;
    }
    public String convertToJson(){
        String json = null;
        // 1. Initialize the Mapper
        ObjectMapper mapper = new ObjectMapper();

        // 2. Register the module to handle LocalTime
        mapper.registerModule(new JavaTimeModule());

        // 3. Make the JSON look "pretty" (indented)
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        try {
            // Convert the object to a String
            json = mapper.writeValueAsString(this);

            // Print it or save it to a file
            System.out.println(json);

            // To save to a file:
            // mapper.writeValue(new File("best_schedule.json"), bestSchedule);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
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