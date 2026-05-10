package com.kaya.algorithm;

import com.kaya.model.TimeTable;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Setter
@Getter
public class Island {

    private final int id;
    private ArrayList<TimeTable> population;

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

    public void injectImmigrants(ArrayList<TimeTable> immigrants) {
        int popSize = this.population.size();
        int numImmigrants = immigrants.size();

        for (int i = 0; i < numImmigrants; i++) {
            this.population.remove(popSize - 1 - i);
        }

        this.population.addAll(immigrants);
    }
}
