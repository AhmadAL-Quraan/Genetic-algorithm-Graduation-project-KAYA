package com.kaya.algorithm;

public class GAConfig {
    public int maxGenerations;
    public int populationSize;
    public int elitismCount;
    public int tournamentSize;
    public double initialMutationRate;
    public double mutationImpactRatio;

    public GAConfig() {
        this.maxGenerations      = 500;
        this.populationSize      = 150;
        this.elitismCount        = 2;
        this.tournamentSize      = 5;
        this.initialMutationRate = 0.15;
        this.mutationImpactRatio = 0.20;
    }
}
