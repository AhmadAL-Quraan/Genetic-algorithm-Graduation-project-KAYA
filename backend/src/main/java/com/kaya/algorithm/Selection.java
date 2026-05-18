package com.kaya.algorithm;

import com.kaya.model.TimeTable;

import java.util.List;
import java.util.Random;

/**
 * Utility class responsible for the Selection phase of the Genetic Algorithm.
 * Implements strategies to choose high-quality parents for reproduction.
 */
public class Selection {

    /**
     * Implements Tournament Selection.
     * Randomly picks a specified number of individuals (tournamentSize) from the population
     * and selects the one with the best fitness score to become a parent.
     * This strategy effectively balances selection pressure with genetic diversity.
     *
     * @param population     The current generation of TimeTables.
     * @param tournamentSize The number of random chromosomes to compete in the tournament.
     * @return The TimeTable (Chromosome) with the best fitness score from the selected subset.
     */
    public static TimeTable tournamentSelection(List<TimeTable> population, int tournamentSize) {

        TimeTable best = null;
        Random rand = new Random();

        for (int i = 0; i < tournamentSize; i++) {
            // Select a random individual from the entire population
            TimeTable ind = population.get(rand.nextInt(population.size()));

            // Compare fitness scores.
            // Note: Fitness score is encapsulated, so we strictly use the getter.
            // A higher fitness score (closer to 0) indicates a better schedule.
            if (best == null || ind.getReport().getTotalPenalty() > best.getReport().getTotalPenalty()) {
                best = ind;
            }
        }
        return best;
    }
}