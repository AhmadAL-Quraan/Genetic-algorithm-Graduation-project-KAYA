package org.example.timetable.algorithm;

import org.example.timetable.model.*;
import org.example.timetable.model.enums.RoomType;

import java.util.*;

/**
 * Clean Genetic Algorithm Class
 * The maestro responsible for managing generations and evolution.
 */
public class GeneticAlgorithm {

    private final int populationSize;
    private final int maxGenerations;
    private final int tournamentSize;
    private final double mutationRate;
    private final int elitismCount;

    public GeneticAlgorithm(int populationSize, int maxGenerations, int tournamentSize, double mutationRate, int elitismCount) {
        this.populationSize = populationSize;
        this.maxGenerations = maxGenerations;
        this.tournamentSize = tournamentSize;
        this.mutationRate = mutationRate;
        this.elitismCount = elitismCount;
    }

    public Schedule evolve(List<CourseOffering> baseOfferings, Map<RoomType, List<Room>> roomPools, List<TimeSlot> onSiteTimePool, List<TimeSlot> onlineTimePool) {
        // 1. Initialization
        List<Schedule> population = new ArrayList<>();
        for (int i = 0; i < populationSize; i++) {
            // Clone offerings for each chromosome
            List<CourseOffering> clonedOfferings = cloneOfferings(baseOfferings);
            Schedule schedule = new Schedule(clonedOfferings);
            // Pass both time pools
            schedule.initializeRandomly(roomPools, onSiteTimePool, onlineTimePool);
            population.add(schedule);
        }

        // 2. Evolution Loop
        for (int gen = 1; gen <= maxGenerations; gen++) {
            // Sort to keep the best at the beginning (closest to 0)
            population.sort((a, b) -> Integer.compare(b.getFitness(), a.getFitness()));

            System.out.println("Generation " + gen + " | Best Fitness: " + population.get(0).getFitness());

            // Stop early if perfect schedule is found
            if (population.get(0).getFitness() == 0) {
                System.out.println("--- Perfect Schedule Found! ---");
                break;
            }

            List<Schedule> nextGeneration = new ArrayList<>();

            // A. Elitism: Keep the best schedules from the previous generation
            for (int i = 0; i < elitismCount; i++) {
                nextGeneration.add(population.get(i));
            }

            // B. Reproduction
            while (nextGeneration.size() < populationSize) {
                // Select parents
                Schedule parent1 = tournamentSelection(population);
                Schedule parent2 = tournamentSelection(population);

                // Crossover
                Schedule child = Schedule.crossover(parent1, parent2);

                // Mutation (Pass both time pools)
                if (Math.random() < mutationRate) {
                    child.mutate(roomPools, onSiteTimePool, onlineTimePool);
                }

                nextGeneration.add(child);
            }

            population = nextGeneration;
        }

        // Final sort to return the best schedule found
        population.sort((a, b) -> Integer.compare(b.getFitness(), a.getFitness()));
        return population.get(0);
    }

    /**
     * Tournament Selection
     */
    private Schedule tournamentSelection(List<Schedule> population) {
        Random rand = new Random();
        Schedule best = null;
        for (int i = 0; i < tournamentSize; i++) {
            Schedule ind = population.get(rand.nextInt(population.size()));
            if (best == null || ind.getFitness() > best.getFitness()) {
                best = ind;
            }
        }
        return best;
    }

    /**
     * Helper method to deep copy offerings so each schedule is independent
     */
    private List<CourseOffering> cloneOfferings(List<CourseOffering> original) {
        List<CourseOffering> cloned = new ArrayList<>();
        for (CourseOffering off : original) {
            cloned.add(new CourseOffering(off.getId(), off.getCourse(), off.getInstructor()));
        }
        return cloned;
    }
}