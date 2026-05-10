package com.kaya.algorithm;

import com.kaya.model.FitnessReport;
import com.kaya.model.Lecture;
import com.kaya.model.Room;
import com.kaya.model.TimeSlot;
import com.kaya.model.TimeTable;
import com.kaya.model.enums.RoomType;
import com.kaya.model.enums.TeachingMethod;

import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class EvolutionEngine {
    private final GAConfig config;
    private double currentMutationRate;

    public EvolutionEngine(GAConfig config) {
        this.config = config;
        this.currentMutationRate = config.initialMutationRate;
    }

    public ArrayList<TimeTable> initializePopulation(ArrayList<Lecture> lectures,
                                                     Map<TeachingMethod, HashSet<TimeSlot>> timePools,
                                                     Map<RoomType, HashSet<Room>> roomPools) {
        ArrayList<TimeTable> population = new ArrayList<>();

        for (int i = 0; i < config.populationSize; i++) {
            ArrayList<Lecture> individualClasses = new ArrayList<>();
            for (Lecture c : lectures) {
                Lecture copy = new Lecture();
                copy.setId(c.getId());
                copy.setCourse(c.getCourse());
                copy.setSectionNumber(c.getSectionNumber());
                copy.setTeacher(c.getTeacher());
                copy.setInstructor(c.getInstructor());
                individualClasses.add(copy);
            }
            TimeTable tt = new TimeTable(individualClasses);
            TimeTableInitializer.initializeRandomly(tt, timePools, roomPools);
            population.add(tt);
        }
        return population;
    }

    public ArrayList<TimeTable> evolveGenerations(ArrayList<TimeTable> population,
                                                  Map<TeachingMethod, HashSet<TimeSlot>> timePools,
                                                  Map<RoomType, HashSet<Room>> roomPools,
                                                  BooleanSupplier cancelCheck,
                                                  Consumer<ProgressSnapshot> progressCallback) {
        int unevolvedGenerations = 0;

        for (int gen = 1; gen <= config.maxGenerations; gen++) {

            if (cancelCheck != null && cancelCheck.getAsBoolean()) {
                throw new RuntimeException("CANCELLED");
            }

            population.sort((a, b) -> Long.compare(b.getReport().getTotalPenalty(), a.getReport().getTotalPenalty()));

            TimeTable best = population.get(0);
            int bestFitness = best.getReport().getTotalPenalty();
            FitnessReport report = best.getReport();

            System.out.println("Generation " + gen + " | Best Fitness: " + bestFitness
                    + " | MutationRate: " + currentMutationRate);

            if (progressCallback != null) {
                progressCallback.accept(new ProgressSnapshot(
                        gen,
                        config.maxGenerations,
                        bestFitness,
                        report.getRoomConflicts() != null ? report.getRoomConflicts() : 0,
                        report.getInstructorConflicts() != null ? report.getInstructorConflicts() : 0,
                        report.getStudentConflicts() != null ? report.getStudentConflicts() : 0,
                        currentMutationRate,
                        bestFitness == 0
                ));
            }

            if (bestFitness == 0) {
                System.out.println("--- Perfect Schedule Found! ---");
                break;
            }

            long currentBestFitness = population.get(0).getReport().getTotalPenalty();
            long newGenBestFitness = currentBestFitness;

            ArrayList<TimeTable> nextGen = new ArrayList<>();

            for (int i = 0; i < config.elitismCount; i++) {
                nextGen.add(population.get(i));
            }

            while (nextGen.size() < config.populationSize) {
                TimeTable p1 = Selection.tournamentSelection(population, config.tournamentSize);
                TimeTable p2 = Selection.tournamentSelection(population, config.tournamentSize);
                TimeTable child = GeneticOperators.crossover(p1, p2);
                FitnessCalculator.calculateFitness(child);

                if (Math.random() < currentMutationRate) {
                    GeneticOperators.mutate(child, timePools, roomPools, config.mutationImpactRatio);
                    FitnessCalculator.calculateFitness(child);
                }

                if (child.getReport().getTotalPenalty() > newGenBestFitness) {
                    newGenBestFitness = child.getReport().getTotalPenalty();
                }

                nextGen.add(child);
            }

            if (currentBestFitness >= newGenBestFitness) {
                unevolvedGenerations++;
            } else {
                unevolvedGenerations = 0;
                currentMutationRate = config.initialMutationRate;
            }

            if (unevolvedGenerations > 0 && unevolvedGenerations % 20 == 0 && currentMutationRate < 0.5) {
                currentMutationRate += 0.10;
                System.out.println("  [Adaptive] Mutation rate boosted to " + currentMutationRate);
            }

            population = nextGen;
        }

        population.sort((a, b) -> Long.compare(b.getReport().getTotalPenalty(), a.getReport().getTotalPenalty()));
        return population;
    }
}
