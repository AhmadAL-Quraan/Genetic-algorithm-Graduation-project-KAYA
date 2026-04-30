package com.kaya.algorithm;

import com.kaya.model.Lecture;
import com.kaya.model.Room;
import com.kaya.model.TimeSlot;
import com.kaya.model.TimeTable;
import com.kaya.model.RoomType;
import com.kaya.model.TimeSlotType;

import java.util.*;

/**
 * The core Genetic Engine responsible for managing the entire evolution lifecycle.
 * Architected to be completely Stateless and Thread-Safe, ensuring that multiple
 * users (e.g., different university admins) can trigger schedule generation
 * concurrently without any thread-interference or state leakage.
 */
public class EvolutionEngine {

    // Configuration object holding all GA parameters. Marked as final to prevent reassignment.
    private final GAConfig config;

    /**
     * Initializes the engine with the provided genetic algorithm configurations.
     * * @param config The configuration object containing parameters like population size,
     * mutation rate, max generations, etc.
     */
    public EvolutionEngine(GAConfig config) {
        this.config = config;
    }

    /**
     * Phase 1: Generates the initial random population (Generation 0).
     * * @param lectures  The master list of all lectures to be scheduled.
     * @param timePools A categorized map of available time slots.
     * @param roomPools A categorized map of available rooms.
     * @return An ArrayList containing the initially generated random TimeTables.
     */
    public ArrayList<TimeTable> initializePopulation(ArrayList<Lecture> lectures,
                                                     Map<TimeSlotType, HashSet<TimeSlot>> timePools,
                                                     Map<RoomType, HashSet<Room>> roomPools) {

        ArrayList<TimeTable> population = new ArrayList<>();

        for (int i = 0; i < config.populationSize; i++) {
            ArrayList<Lecture> individualClasses = new ArrayList<>();

            // Deep Copy: We instantiate new Lecture objects for each TimeTable.
            // This prevents memory overlap (pass-by-reference issues) where mutating
            // a lecture in one schedule accidentally mutates it in another.
            for (Lecture c : lectures) {
                individualClasses.add(new Lecture(c.getId(), c.getCourse(), null, null, c.getSectionNumber(), c.getInstructor()));
            }
            TimeTable tt = new TimeTable(individualClasses);

            // Assign random but requirement-compliant times and rooms to each lecture
            TimeTableInitializer.initializeRandomly(tt, timePools, roomPools);
            population.add(tt);
        }
        return population;
    }

    /**
     * Phase 2: The evolutionary loop. Applies selection, crossover, and mutation
     * across multiple generations to progressively find the optimal clash-free schedule.
     * * @param population The initial generation of timetables.
     * @param timePools  A categorized map of available time slots.
     * @param roomPools  A categorized map of available rooms.
     * @return The final evolved population, sorted by fitness (best schedule at index 0).
     */
    public ArrayList<TimeTable> evolveGenerations(ArrayList<TimeTable> population,
                                                  Map<TimeSlotType, HashSet<TimeSlot>> timePools,
                                                  Map<RoomType, HashSet<Room>> roomPools) {

        int unevolvedGenerations = 0;

        // Local variable for thread-safety. Allows dynamic mutation adjustment
        // without affecting concurrent generation requests.
        double currentMutationRate = config.initialMutationRate;

        // The main Evolutionary Loop
        for (int gen = 1; gen <= config.maxGenerations; gen++) {

            // 1. Evaluation & Sorting: Rank schedules from best (fitness closest to 0) to worst.
            population.sort((a, b) -> Long.compare(b.getFitness(), a.getFitness()));
            System.out.println("Generation " + gen + " | Best Fitness: " + population.get(0).getFitness());

            // Early Stopping Condition: If a flawless schedule is found, halt evolution to save resources.
            if (population.get(0).getFitness() == 0) {
                System.out.println("--- Perfect Schedule Found! ---");
                break;
            }

            ArrayList<TimeTable> nextGen = new ArrayList<>();

            // 2. ELITISM: Carry over the absolute best schedules to the next generation unchanged.
            // This guarantees that the algorithm never loses its best-found solutions.
            for (int i = 0; i < config.elitismCount; i++) {
                nextGen.add(population.get(i));
            }

            // 3. REPRODUCTION: Fill the remainder of the new generation via Selection & Crossover.
            while (nextGen.size() < config.populationSize) {
                // Tournament Selection: Pick strong parents from the population.
                TimeTable p1 = Selection.tournamentSelection(population, config.tournamentSize);
                TimeTable p2 = Selection.tournamentSelection(population, config.tournamentSize);

                // Crossover: Combine genetic material of parents to create an offspring.
                TimeTable child = GeneticOperators.crossover(p1, p2);

                // Initial child evaluation to identify conflicting lectures before mutation.
                FitnessCalculator.calculateFitness(child);

                // 4. MUTATION: Inject random alterations to resolve existing conflicts.
                if (Math.random() < currentMutationRate) {
                    GeneticOperators.mutate(child, timePools, roomPools, config.mutationImpactRatio);
                }

                // Final evaluation after potential mutation to update the fitness score.
                FitnessCalculator.calculateFitness(child);
                nextGen.add(child);
            }

            // 5. ADAPTIVE MUTATION LOGIC: Prevents stagnation in Local Optima.
            // If the best fitness score hasn't improved compared to the previous generation,
            // we increment the stagnation counter.
            if (population.get(0).getFitness() >= nextGen.get(0).getFitness()) {
                unevolvedGenerations++;
            } else {
                // Reset counter and restore baseline mutation rate upon improvement.
                unevolvedGenerations = 0;
                currentMutationRate = config.initialMutationRate;
            }

            // If the algorithm has stagnated for 50 generations, incrementally boost the
            // mutation rate (up to a 50% cap) to forcefully introduce genetic diversity.
            if (unevolvedGenerations > 0 && unevolvedGenerations % 50 == 0 && currentMutationRate < 0.5) {
                currentMutationRate += 0.05;
            }

            // Replace the old generation with the newly evolved generation.
            population = nextGen;
        }

        // Final sort to ensure the absolute best schedule rests at index 0 before returning.
        population.sort((a, b) -> Long.compare(b.getFitness(), a.getFitness()));
        return population;
    }
}