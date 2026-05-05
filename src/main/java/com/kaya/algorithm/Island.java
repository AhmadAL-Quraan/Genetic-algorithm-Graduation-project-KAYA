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

    private final int id;
    private ArrayList<TimeTable> population;

    // Island-specific adaptive mutation state
    private int unevolvedGenerations;
    private double currentMutationRate;
    private long generationBestFitness;

    private boolean perfectScheduleFound;

    public Island(int id, ArrayList<TimeTable> initialPopulation, double initialMutationRate) {
        this.id = id;
        this.population = initialPopulation;
        this.currentMutationRate = initialMutationRate;
        this.unevolvedGenerations = 0;
        this.generationBestFitness = Long.MIN_VALUE;
        this.perfectScheduleFound = false;
    }

    /**
     * Replaces the worst performing schedules in this island with immigrants.
     * @param immigrants Deep copies of top schedules from a neighboring island.
     */
    public void injectImmigrants(ArrayList<TimeTable> immigrants) {
        // Population is assumed to be sorted (best at index 0, worst at the end).
        int popSize = this.population.size();
        int numImmigrants = immigrants.size();

        // Remove the worst schedules from the bottom
        for (int i = 0; i < numImmigrants; i++) {
            this.population.remove(popSize - 1 - i);
        }

        // Add the strong immigrants
        this.population.addAll(immigrants);
    }
}