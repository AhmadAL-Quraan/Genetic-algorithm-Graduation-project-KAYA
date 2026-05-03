package com.kaya.algorithm.run;

import com.kaya.algorithm.EvolutionEngine;
import com.kaya.algorithm.GAConfig;
import com.kaya.algorithm.IslandManager;
import com.kaya.model.Lecture;
import com.kaya.model.Room;
import com.kaya.model.TimeSlot;
import com.kaya.model.TimeTable;
import com.kaya.model.enums.RoomType;
import com.kaya.model.enums.TeachingMethod;

import java.util.*;

/**
 * The primary entry point for the KAYA Genetic Algorithm.
 * Acts as the bridge between the Spring Boot Service layer and the core optimization engine.
 * Supports both the Legacy Single-Population Engine and the Advanced Island Model Architecture.
 */
public class StartPoint {

    /**
     * Executes the Genetic Algorithm to generate an optimal, clash-free timetable.
     * Designed to be invoked by the Backend controller after fetching necessary data.
     *
     * @param lectures       The complete list of lectures (course instances) to be scheduled.
     * @param allRooms       The list of all available physical rooms in the university.
     * @param allTimeSlots   The dynamically generated list of time slots based on user preferences.
     * @param config         The GA configuration object (Default or customized via the React UI).
     * @param useIslandModel Feature flag: true to activate the Parallel Island Model, false for the Legacy Engine.
     * @return The most optimal TimeTable (Chromosome) found, representing the final schedule.
     */
    public static TimeTable runAlgorithm(List<Lecture> lectures, List<Room> allRooms, List<TimeSlot> allTimeSlots, GAConfig config, boolean useIslandModel) {

        System.out.println("Starting KAYA Timetable Scheduler Engine...");

        // 1. Data Preparation (Dynamic Pooling)
        Map<RoomType, HashSet<Room>> roomPools = new HashMap<>();

        // [RESTORED]: Reverted back to TeachingMethod Enum as per the latest architectural decision
        Map<TeachingMethod, HashSet<TimeSlot>> timePools = new HashMap<>();

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
            // Fetching TeachingMethod to match the updated Data Models
            TeachingMethod type = ts.getTeachingMethod();
            if (type != null) {
                timePools.computeIfAbsent(type, k -> new HashSet<>()).add(ts);
            }
        }

        TimeTable bestSchedule;

        // 2. Execution Routing based on the Feature Toggle
        if (useIslandModel) {
            // =================================================================================
            // --- NEW ADVANCED ISLAND MODEL ARCHITECTURE ---
            // =================================================================================
            System.out.println("Feature Toggle: ENABLED -> Routing to the Island Manager...");

            // The IslandManager encapsulates the initialization, epoch loops, and parallel migrations.
            IslandManager islandManager = new IslandManager(config);

            // This single call triggers the Parallel Streams and returns the absolute global best schedule.
            bestSchedule = islandManager.runEvolution(new ArrayList<>(lectures), timePools, roomPools);

        } else {
            // =================================================================================
            // --- LEGACY SINGLE-POPULATION ENGINE ---
            // =================================================================================
            System.out.println("Feature Toggle: DISABLED -> Routing to the Legacy Evolution Engine...");

            // Initialize the legacy engine with the injected Configuration
            EvolutionEngine engine = new EvolutionEngine(config);

            System.out.println("Initializing Generation 0 (Random Population)...");
            // Assuming the legacy initializePopulation signature matches this setup
            ArrayList<TimeTable> initialPop = engine.initializePopulation(new ArrayList<>(lectures), timePools, roomPools);

            System.out.println("Starting the Standard Evolution Process...");
            ArrayList<TimeTable> finalPop = engine.evolveGenerations(initialPop, timePools, roomPools);

            // The evolveGenerations method returns the population sorted by fitness.
            // Therefore, the schedule at index 0 is the absolute best solution found.
            bestSchedule = finalPop.get(0);
        }

        // 3. Final Output & Reporting
        System.out.println("=====================================");
        System.out.println("Evolution Complete. Global Best Fitness Report:");
        System.out.println(bestSchedule.getReport());
        System.out.println("=====================================");

        // Return the best schedule back to the Spring Boot Service for DB persistence or API response.
        return bestSchedule;
    }
}