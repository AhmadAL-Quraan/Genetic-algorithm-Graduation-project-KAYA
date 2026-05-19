package com.kaya.algorithm;

import com.kaya.model.TimeTable;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Setter
@Getter
/**
 * Represents a single, isolated sub-population in the Island Model GA.
 * Encapsulates the state of evolution for this specific island,
 * ensuring 100% thread-safety when processed by Parallel Streams.
 */
public class Island {

    /**
     * Unique identifier for the island (e.g., 0, 1, 2).
     * Useful for debugging and tracking which island found the best solution.
     */
    private final int id;

    /**
     * The isolated subset of timetables evolving exclusively within this island.
     * Prevents premature convergence by maintaining local genetic diversity.
     */
    private ArrayList<TimeTable> population;

    // =========================================================================
    // --- ISLAND-SPECIFIC ADAPTIVE MUTATION STATE ---
    // These variables allow each island to manage its own stagnation and mutation
    // rates independently, preventing race conditions during parallel execution.
    // =========================================================================

    /**
     * Counter tracking how many consecutive generations this island has failed
     * to improve its best fitness score (Stagnation indicator).
     */
    private int unevolvedGenerations;

    /**
     * The dynamic mutation probability for this island.
     * Increases automatically if the island stagnates to force genetic exploration.
     */
    private double currentMutationRate;

    /**
     * Records the highest fitness score achieved by this island so far.
     * Used to detect improvements and reset the unevolvedGenerations counter.
     */
    private long generationBestFitness;

    // =========================================================================

    /**
     * A global flag for Early Stopping.
     * If true, it signals the IslandManager that a flawless schedule (Fitness = 0)
     * has been found, allowing all parallel threads to halt immediately to save CPU cycles.
     */
    private boolean perfectScheduleFound;

    /**
     * Constructs a new Island with its initial population and base mutation rate.
     */
    public Island(int id, ArrayList<TimeTable> initialPopulation, double initialMutationRate) {
        this.id = id;
        this.population = initialPopulation;
        this.currentMutationRate = initialMutationRate;
        this.unevolvedGenerations = 0;
        this.generationBestFitness = Long.MIN_VALUE;
        this.perfectScheduleFound = false;
    }

    /**
     * Handles the Migration phase.
     * Replaces the weakest/worst performing schedules in this island with
     * elite immigrants from a neighboring island to boost genetic quality.
     * * @param immigrants Deep copies of the top schedules from another island.
     */
    public void injectImmigrants(ArrayList<TimeTable> immigrants) {
        // Assumption: The population array is already sorted by fitness in descending order
        // (Index 0 is the best schedule, the last index is the worst).
        int popSize = this.population.size();
        int numImmigrants = immigrants.size();

        // 1. Cull the weak: Remove the worst schedules from the bottom (tail) of the list
        for (int i = 0; i < numImmigrants; i++) {
            this.population.remove(popSize - 1 - i);
        }

        // 2. Introduce new blood: Add the elite immigrants to the island's population
        this.population.addAll(immigrants);
    }
}