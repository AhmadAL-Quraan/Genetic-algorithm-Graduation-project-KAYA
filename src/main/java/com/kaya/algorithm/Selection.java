package com.kaya.algorithm;

import com.kaya.model.TimeTable;

import java.util.List;
import java.util.Random;

public class Selection {

    // Tournament Selection: Picks the best TimeTable out of tournamentSize random individuals from the population.
    public static TimeTable tournamentSelection(List<TimeTable> population, int tournamentSize) {

        /*
        // =============== الكود القديم ===============
        TimeTable best = null;
        Random rand = new Random();
        for (int i = 0; i < tournamentSize; i++) {
            TimeTable ind = population.get(rand.nextInt(population.size()));
            if (best == null || ind.fitness > best.fitness) {
                    best = ind;
            }
        }
        return best;
        // ============================================
        */

        // =============== الكود الجديد ===============
        // الفكرة: بنختار عدد معين (tournamentSize) عشوائياً من الجيل (Population)
        // وبنرجع أحسن جدول فيهم من ناحية الـ Fitness Score عشان يتزاوج.
        TimeTable best = null;
        Random rand = new Random();
        for (int i = 0; i < tournamentSize; i++) {
            TimeTable ind = population.get(rand.nextInt(population.size()));

            // [تم التعديل]: استخدمنا getFitness() بدل .fitness لأننا احترمنا الـ Encapsulation وخليناه private
            if (best == null || ind.getFitness() > best.getFitness()) {
                best = ind;
            }
        }
        return best;
    }
}