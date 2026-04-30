package com.kaya.algorithm;

/**
 * Implements the Configuration Object Pattern.
 * Centralizes all Genetic Algorithm parameters into a single, clean data structure.
 * Designed to either accept dynamic adjustments from the User Interface (React)
 * or fall back to empirically proven default settings.
 */
public class GAConfig {
    public int maxGenerations;
    public int populationSize;
    public int elitismCount;
    public int tournamentSize;
    public double initialMutationRate;

    // Controls the intensity of the mutation operation.
    // Defines the percentage of conflicting courses to be altered when a mutation occurs (e.g., 0.10 = 10%).
    public double mutationImpactRatio;

    /**
     * Default Constructor.
     * Populated with the empirically discovered "Sweet Spot" parameters.
     * These values balance computational efficiency with high solution quality,
     * ensuring an optimal, clash-free timetable is generated in the shortest time.
     */
    public GAConfig() {
        this.maxGenerations = 400;       // Maximum number of evolutionary cycles
        this.populationSize = 100;       // Number of schedules per generation
        this.elitismCount = 2;           // Number of top schedules passed unchanged to the next generation
        this.tournamentSize = 5;         // Subset size for the parent selection competition
        this.initialMutationRate = 0.15; // Base probability of a mutation occurring (15%)
        this.mutationImpactRatio = 0.10; // Mutate 10% of conflicting courses when a mutation triggers
    }

    /**
     * Parameterized Constructor.
     * Utilized when a System Administrator dynamically overrides the default
     * GA configurations via the frontend application.
     *
     * @param maxGenerations      The absolute limit of generations before the algorithm halts.
     * @param populationSize      The number of chromosomes (TimeTables) in a single generation.
     * @param elitismCount        The number of elite individuals preserved across generations.
     * @param tournamentSize      The number of individuals drawn for a selection tournament.
     * @param initialMutationRate The starting probability of a chromosome undergoing mutation.
     * @param mutationImpactRatio The fraction of conflicts targeted during a mutation event.
     */
    public GAConfig(int maxGenerations, int populationSize, int elitismCount,
                    int tournamentSize, double initialMutationRate, double mutationImpactRatio) {
        this.maxGenerations = maxGenerations;
        this.populationSize = populationSize;
        this.elitismCount = elitismCount;
        this.tournamentSize = tournamentSize;
        this.initialMutationRate = initialMutationRate;
        this.mutationImpactRatio = mutationImpactRatio;
    }
}