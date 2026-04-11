package com.kaya.algorithm;

import com.kaya.model.TimeTable;

import java.util.List;
import java.util.Random;

public class Selection {

    // Tournament Selection: Picks the best TimeTable out of tournamentSize random individuals from the population.
    public static TimeTable tournamentSelection(List<TimeTable> population, int tournamentSize) {
        //TimeTable best = population.get(0);
        TimeTable best = null;
        Random rand = new Random();
        for (int i = 0; i < tournamentSize; i++) {
            TimeTable ind = population.get(rand.nextInt(population.size()));
            //TimeTable ind = population.get(i);

            if (best == null || ind.fitness > best.fitness) {
            //if (ind.fitness > best.fitness) {
                    best = ind;
             ///       System.out.println("Best fitness:kyfg " + best.fitness);
            }
        }
        return best;
    }
}