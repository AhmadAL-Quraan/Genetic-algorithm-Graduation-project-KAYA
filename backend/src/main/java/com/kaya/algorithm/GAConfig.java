package com.kaya.algorithm;

public class GAConfig {
    public int maxGenerations;
    public int populationSize;
    public int elitismCount;
    public double elitismRatio;
    public int tournamentSize;
    public double initialMutationRate;
    public double mutationImpactRatio;
    public double stagnationToleranceRatio;

    public GAConfig() {
        this.maxGenerations = 400;
        this.populationSize = 100;
        this.elitismCount = 2;
        this.elitismRatio = 0.02;
        this.tournamentSize = 5;
        this.initialMutationRate = 0.15;
        this.mutationImpactRatio = 0.20;
        this.stagnationToleranceRatio = 0.05;
    }
}
