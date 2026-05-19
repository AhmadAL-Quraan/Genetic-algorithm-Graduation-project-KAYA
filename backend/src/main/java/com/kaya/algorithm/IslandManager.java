package com.kaya.algorithm;

import com.kaya.model.Lecture;
import com.kaya.model.Room;
import com.kaya.model.TimeSlot;
import com.kaya.model.TimeTable;
import com.kaya.model.enums.RoomType;
import com.kaya.model.enums.TeachingMethod;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * The Orchestrator of the Island Model Genetic Algorithm.
 * Manages parallel evolution of multiple islands and facilitates migration
 * to achieve explosive genetic diversity and rapid convergence.
 */
public class IslandManager {

    private final GAConfig config;
    private final EvolutionEngineIsland engine;

    public IslandManager(GAConfig config) {
        this.config = config;
        this.engine = new EvolutionEngineIsland(config);
    }

    /**
     * Executes the complete Island Model evolutionary process.
     */
    public TimeTable runEvolution(ArrayList<Lecture> lectures,
                                  Map<TeachingMethod, HashSet<TimeSlot>> timePools,
                                  Map<RoomType, HashSet<Room>> roomPools) {

        int islandPopSize = config.populationSize / config.numIslands;
        List<Island> islands = new ArrayList<>();

        System.out.println("Initializing " + config.numIslands + " Islands in parallel...");

        // 1. Initialize Islands
        for (int i = 0; i < config.numIslands; i++) {
            ArrayList<TimeTable> initialPop = engine.initializePopulation(lectures, timePools, roomPools, islandPopSize);
            islands.add(new Island(i, initialPop, config.initialMutationRate));
        }

        int epochs = config.maxGenerations / config.migrationInterval;

        // 2. The Epoch Loop
        for (int e = 0; e < epochs; e++) {
            System.out.println("--- Starting Epoch " + (e + 1) + "/" + epochs + " ---");

            // 3. PARALLEL EVOLUTION: Evolve all islands simultaneously using CPU Cores!
            islands.parallelStream().forEach(island -> {
                engine.evolveIslandEpoch(island, config.migrationInterval, timePools, roomPools);
            });

            // Early Stopping Check: Did any island find a flawless schedule?
            if (islands.stream().anyMatch(Island::isPerfectScheduleFound)) {
                System.out.println("🏆 Perfect Schedule Found! Halting evolution early.");
                break;
            }

            // 4. MIGRATION: Ring Topology (Island 0 -> 1, 1 -> 2, ..., N -> 0)
            // Step 4a: Extract deep copies of elites to prevent cascading migrations
            List<ArrayList<TimeTable>> migrantsBuffer = new ArrayList<>();
            for (Island island : islands) {
                migrantsBuffer.add(extractMigrants(island, config.migrationRate));
            }

            // Step 4b: Inject immigrants into the neighboring island
            for (int i = 0; i < islands.size(); i++) {
                Island nextIsland = islands.get((i + 1) % islands.size());
                nextIsland.injectImmigrants(migrantsBuffer.get(i));
            }
        }

        // 5. Global Best Extraction
        // Sort all islands' populations just to be safe, then compare the top schedule of each island.
        TimeTable globalBest = null;
        for (Island island : islands) {
            island.getPopulation().sort((a, b) -> Long.compare(b.getReport().getTotalPenalty(), a.getReport().getTotalPenalty()));
            TimeTable islandBest = island.getPopulation().get(0);

            if (globalBest == null || islandBest.getReport().getTotalPenalty() > globalBest.getReport().getTotalPenalty()) {
                globalBest = islandBest;
            }
        }

        System.out.println("Evolution Complete! Global Best Fitness: " + globalBest.getReport().getTotalPenalty());
        return globalBest;
    }

    /**
     * Extracts deep copies of the top performing schedules from an island.
     * Deep copying is absolutely crucial to prevent memory overlap across islands.
     */
    private ArrayList<TimeTable> extractMigrants(Island island, int count) {
        ArrayList<TimeTable> migrants = new ArrayList<>();
        ArrayList<TimeTable> pop = island.getPopulation();

        for (int i = 0; i < count && i < pop.size(); i++) {
            TimeTable original = pop.get(i);
            ArrayList<Lecture> copiedLectures = new ArrayList<>();

            for (Lecture l : original.getLectures()) {
                copiedLectures.add(new Lecture(l.getId(), l.getCourse(), l.getRoom(), l.getTimeSlot(), l.getSectionNumber(), l.getInstructor()));
            }

            TimeTable copy = new TimeTable(copiedLectures);
            copy.getReport().setTotalPenalty( original.getReport().getTotalPenalty() );
            migrants.add(copy);
        }
        return migrants;
    }
}