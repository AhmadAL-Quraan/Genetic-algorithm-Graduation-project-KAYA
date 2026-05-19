package com.kaya.algorithm;

/**
 * Implements the Configuration Object Pattern.
 * Centralizes all Genetic Algorithm parameters into a single, clean data structure.
 * Designed to either accept dynamic adjustments from the User Interface (React)
 * or fall back to empirically proven default settings.
 */
public class GAConfig {

    public int maxGenerations = 400;
    public int populationSize = 100;
    public int tournamentSize = 5;
    public double elitismRatio = 0.015;
    public double initialMutationRate = 0.15;

    // Controls the intensity of the mutation operation.
    // Defines the percentage of conflicting courses to be altered when a mutation occurs (e.g., 0.10 = 10%).
    public double mutationImpactRatio = 0.10;

    // Defines the tolerance threshold for fitness stagnation (e.g., 0.10 means 10% of total generations).
    // If the algorithm fails to improve for this percentage of generations, the Adaptive Mutation logic is triggered.
    public double stagnationToleranceRatio = 0.10;


    // --- ISLAND MODEL PARAMETERS (NEW) ---
    public int numIslands = 4;          // Number of isolated populations
    public int migrationInterval = 20;   // Number of generations before a migration occurs (Epoch length)
    public int migrationRate = 2;       // Number of elite schedules to transfer between islands


    /**
     * Default Constructor.
     * Populated with the empirically discovered "Sweet Spot" parameters.
     * These values balance computational efficiency with high solution quality,
     * ensuring an optimal, clash-free timetable is generated in the shortest time.
     */
    public GAConfig() {
        this.maxGenerations = 400;       // Maximum number of evolutionary cycles
        this.populationSize = 100;       // Number of schedules per generation
        this.tournamentSize = 5;         // Subset size for the parent selection competition
        this.elitismRatio = 0.015;           // Number of top schedules passed unchanged to the next generation
        this.initialMutationRate = 0.15; // Base probability of a chromosome undergoing mutation (15%)
        this.mutationImpactRatio = 0.10; // Mutate 10% of conflicting courses when a mutation triggers
        this.stagnationToleranceRatio = 0.10; // Trigger adaptive mutation after stagnating for 10% of maxGenerations
        // Island Model Defaults
        this.numIslands = 4;             // Divide 100 schedules into 4 islands (25 each)
        this.migrationInterval = 20;     // Exchange genetic material every 20 generations
        this.migrationRate = 2;          // Transfer the top 2 schedules during migration
    }

    /**
     * Parameterized Constructor.
     * Utilized when a System Administrator dynamically overrides the default
     * GA configurations via the frontend application.
     *
     * @param maxGenerations           The absolute limit of generations before the algorithm halts.
     * @param populationSize           The number of chromosomes (TimeTables) in a single generation.
     * @param elitismRatio             The number of elite individuals preserved across generations.
     * @param tournamentSize           The number of individuals drawn for a selection tournament.
     * @param initialMutationRate      The starting probability of a chromosome undergoing mutation.
     * @param mutationImpactRatio      The fraction of conflicts targeted during a mutation event.
     * @param stagnationToleranceRatio The percentage of max generations to wait before boosting mutation rate.
     */
    public GAConfig(int maxGenerations, int populationSize, int tournamentSize,
                    double elitismRatio, double initialMutationRate, double mutationImpactRatio, double stagnationToleranceRatio) {
        this.maxGenerations = maxGenerations;
        this.populationSize = populationSize;
        this.tournamentSize = tournamentSize;
        this.elitismRatio = elitismRatio;
        this.initialMutationRate = initialMutationRate;
        this.mutationImpactRatio = mutationImpactRatio;
        this.stagnationToleranceRatio = stagnationToleranceRatio;
    }
}