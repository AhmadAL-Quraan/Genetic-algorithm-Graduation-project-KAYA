package com.kaya.algorithm;

import com.kaya.model.TimeTable;

import java.util.List;
import java.util.Random;

public class Selection {

    public static TimeTable tournamentSelection(List<TimeTable> population, int tournamentSize) {
        TimeTable best = null;
        Random rand = new Random();
        for (int i = 0; i < tournamentSize; i++) {
            TimeTable ind = population.get(rand.nextInt(population.size()));
            if (best == null || ind.getReport().getTotalPenalty() > best.getReport().getTotalPenalty()) {
                best = ind;
            }
        }
        return best;
    }
}
