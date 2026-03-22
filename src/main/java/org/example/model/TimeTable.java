package org.example.model;

import org.example.model.*;

import java.util.*;

class TimeTable {
    public ArrayList<Class> classes;
    public int fitness;

    TimeTable(ArrayList<Class> classes) {
        this.classes = classes;
        this.fitness = 0;
    }

    public void mutate(Map<String, Set<TimeSlot>> timePools, Map<String, Set<Room>> roomPools) {
        List<TimeSlot> timePool;
        List<Room> roomPool;

        Random rand = new Random();
        // Pick ONE random class to change
        Class randomClass = this.classes.get(rand.nextInt(this.classes.size()));
        timePool = new ArrayList<>(timePools.get(randomClass.course.timeGroup));
        roomPool = new ArrayList<>(roomPools.get(randomClass.course.roomGroup));
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

    public void initializeRandomly(Map<String, Set<TimeSlot>> timePools, Map<String, Set<Room>> roomPools) {
        List<TimeSlot> timePool;
        List<Room> roomPool;


        Random rand = new Random();
        for (Class c : this.classes) {
            timePool = new ArrayList<>(timePools.get(c.course.timeGroup));
            roomPool = new ArrayList<>(roomPools.get(c.course.roomGroup));
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