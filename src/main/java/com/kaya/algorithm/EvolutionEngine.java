package com.kaya.algorithm;

import com.kaya.model.Lecture;
import com.kaya.model.Room;
import com.kaya.model.TimeSlot;
import com.kaya.model.TimeTable;

import java.util.*;

// حولنا الكلاس من Static Utility إلى Object حقيقي
public class EvolutionEngine {
    private final GAConfig config;
    private double currentMutationRate;

    // الـ Constructor بياخد الإعدادات
    public EvolutionEngine(GAConfig config) {
        this.config = config;
        this.currentMutationRate = config.initialMutationRate;
    }

    public ArrayList<TimeTable> initializePopulation(ArrayList<Lecture> lectures, Map<String, HashSet<TimeSlot>> timePools, Map<String, HashSet<Room>> roomPools) {
        ArrayList<TimeTable> population = new ArrayList<>();
        for (int i = 0; i < config.populationSize; i++) {
            ArrayList<Lecture> individualClasses = new ArrayList<>();
            for (Lecture c : lectures) {
                individualClasses.add(new Lecture(c.getId(),c.getCourse(), null, null, c.getNumber(), c.getInstructor()));
            }
            TimeTable tt = new TimeTable(individualClasses);
            TimeTableInitializer.initializeRandomly(tt, timePools, roomPools);
            population.add(tt);
        }
        return population;
    }

    public ArrayList<TimeTable> evolveGenerations(ArrayList<TimeTable> population, Map<String, HashSet<TimeSlot>> timePools, Map<String, HashSet<Room>> roomPools) {
        int unevolvedGenerations = 0;

        for (int gen = 1; gen <= config.maxGenerations; gen++) {
            population.sort((a, b) -> Long.compare(b.getFitness(), a.getFitness()));
            System.out.println("Generation " + gen + " | Best Fitness: " + population.get(0).fitness);

            if (population.get(0).fitness == 0) {
                System.out.println("--- Perfect Schedule Found! ---");
                break;
            }

            ArrayList<TimeTable> nextGen = new ArrayList<>();

            // ELITISM
            for (int i = 0; i < config.elitismCount; i++) {
                nextGen.add(population.get(i));
            }

            // REPRODUCTION
            while (nextGen.size() < config.populationSize) {
                TimeTable p1 = Selection.tournamentSelection(population, config.tournamentSize);
                TimeTable p2 = Selection.tournamentSelection(population, config.tournamentSize);

                TimeTable child = GeneticOperators.crossover(p1, p2);
                FitnessCalculator.calculateFitness(child);

                // MUTATION (استخدام الميوتيشن ريت الحالي)
                if (Math.random() < currentMutationRate) {
                    //GeneticOperators.mutate(child, timePools, roomPools);
                    GeneticOperators.mutate(child, timePools, roomPools, config.mutationImpactRatio);
                }

                FitnessCalculator.calculateFitness(child);
                nextGen.add(child);
            }

            // Adaptive Mutation Logic
            if (population.get(0).fitness >= nextGen.get(0).fitness) {
                unevolvedGenerations++;
            } else {
                unevolvedGenerations = 0;
                currentMutationRate = config.initialMutationRate; // نرجعها للرقم الأصلي
            }

            if (unevolvedGenerations > 0 && unevolvedGenerations % 50 == 0 && currentMutationRate < 0.5) {
                currentMutationRate += 0.05; // نزود الميوتيشن عشان نخرج من الـ Local Optima
            }

            population = nextGen;
        }

        population.sort((a, b) -> Long.compare(b.getFitness(), a.getFitness()));
        return population;
    }
}