package com.kaya.algorithm.run;

import com.kaya.algorithm.EvolutionEngine;
import com.kaya.algorithm.GAConfig;
import com.kaya.model.Lecture;
import com.kaya.model.Room;
import com.kaya.model.TimeSlot;
import com.kaya.model.TimeTable;
import com.kaya.model.RoomType;
import com.kaya.model.TimeSlotType;

import java.util.*;

/**
 * The primary entry point for the KAYA Genetic Algorithm.
 * Acts as the bridge between the Spring Boot Service layer and the core optimization engine.
 */
public class StartPoint {

    /**
     * Executes the Genetic Algorithm to generate an optimal, clash-free timetable.
     * Designed to be invoked by the Backend controller after fetching necessary data.
     *
     * @param lectures     The complete list of lectures (course instances) to be scheduled.
     * @param allRooms     The list of all available physical rooms in the university.
     * @param allTimeSlots The dynamically generated list of time slots based on user preferences.
     * @param config       The GA configuration object (Default or customized via the React UI).
     * @return The most optimal TimeTable (Chromosome) found, representing the final schedule.
     */
    public static TimeTable runAlgorithm(List<Lecture> lectures, List<Room> allRooms, List<TimeSlot> allTimeSlots, GAConfig config) {

        System.out.println("Starting KAYA Timetable Scheduler Engine...");

        // 1. Data Preparation (Dynamic Pooling)
        Map<RoomType, HashSet<Room>> roomPools = new HashMap<>();
        Map<TimeSlotType, HashSet<TimeSlot>> timePools = new HashMap<>();

        /*
         * Performance Optimization: Using computeIfAbsent instead of putIfAbsent + get.
         * This performs the check and insertion in a single O(1) operation.
         * Switched from LinkedHashSet to HashSet for reduced memory footprint,
         * as insertion order is irrelevant for random genetic selection.
         */
        for (Room room : allRooms) {
            RoomType type = room.getRoomType();
            if (type != null) {
                roomPools.computeIfAbsent(type, k -> new HashSet<>()).add(room);
            }
        }

        for (TimeSlot ts : allTimeSlots) {
            TimeSlotType method = ts.getTimeSlotType();
            if (method != null) {
                timePools.computeIfAbsent(method, k -> new HashSet<>()).add(ts);
            }
        }

        // 2. Initialize Engine with the injected Configuration
        EvolutionEngine engine = new EvolutionEngine(config);

        // 3. Execution Phase
        System.out.println("Initializing Generation 0 (Random Population)...");
        ArrayList<TimeTable> initialPop = engine.initializePopulation(new ArrayList<>(lectures), timePools, roomPools);

        System.out.println("Starting the Evolution Process...");
        ArrayList<TimeTable> finalPop = engine.evolveGenerations(initialPop, timePools, roomPools);

        // 4. Results Extraction
        // The evolveGenerations method returns the population sorted by fitness.
        // Therefore, the schedule at index 0 is the absolute best solution found.
        TimeTable bestSchedule = finalPop.get(0);

        System.out.println("=====================================");
        System.out.println("Evolution Complete. Best Fitness Report:");
        System.out.println(bestSchedule.getReport());
        System.out.println("=====================================");

        // Return the best schedule back to the Spring Boot Service for DB persistence or API response.
        return bestSchedule;
    }
}