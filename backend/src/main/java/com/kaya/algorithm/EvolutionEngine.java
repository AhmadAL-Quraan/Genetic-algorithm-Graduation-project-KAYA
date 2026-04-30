package com.kaya.algorithm;

import com.kaya.dto.response.GenerationProgress;
import com.kaya.model.Lecture;
import com.kaya.model.Room;
import com.kaya.model.TimeSlot;
import com.kaya.model.TimeTable;
import com.kaya.model.enums.RoomType;
import com.kaya.model.enums.TeachingMethod;

import java.util.*;
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
                individualClasses.add(new Lecture(c.getId(), c.getCourse(), null, null, c.getTeacher(), c.getSectionNumber(), c.getInstructor()));
            }
            TimeTable tt = new TimeTable(individualClasses);
            TimeTableInitializer.initializeRandomly(tt, timePools, roomPools);
            population.add(tt);
        }
        return population;
    }

    public ArrayList<TimeTable> evolveGenerations(ArrayList<TimeTable> population,
                                                  Map<TeachingMethod, HashSet<TimeSlot>> timePools,
                                                  Map<RoomType, HashSet<Room>> roomPools) {
        return evolveGenerations(population, timePools, roomPools, null);
    }

    public ArrayList<TimeTable> evolveGenerations(ArrayList<TimeTable> population,
                                                  Map<TeachingMethod, HashSet<TimeSlot>> timePools,
                                                  Map<RoomType, HashSet<Room>> roomPools,
                                                  Consumer<GenerationProgress> progressCallback) {
        int unevolvedGenerations = 0;

        for (int gen = 1; gen <= config.maxGenerations; gen++) {
            population.sort((a, b) -> Long.compare(b.getFitness(), a.getFitness()));
            TimeTable best = population.get(0);
            System.out.println("Generation " + gen + " | Best Fitness: " + best.getFitness());

            if (progressCallback != null) {
                var report = best.getReport();
                long room = report != null ? report.getRoomConflicts() : 0L;
                long instr = report != null ? report.getInstructorConflicts() : 0L;
                long student = report != null ? report.getStudentConflicts() : 0L;

                if (best.getFitness() == 0) {
                    progressCallback.accept(GenerationProgress.perfect(gen, config.maxGenerations));
                } else {
                    progressCallback.accept(GenerationProgress.evolving(
                            gen, config.maxGenerations, best.getFitness(),
                            room, instr, student, currentMutationRate));
                }
            }

            if (best.getFitness() == 0) {
                System.out.println("--- Perfect Schedule Found! ---");
                break;
            }

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
                }
                FitnessCalculator.calculateFitness(child);
                nextGen.add(child);
            }

            if (population.get(0).getFitness() >= nextGen.get(0).getFitness()) {
                unevolvedGenerations++;
            } else {
                unevolvedGenerations = 0;
                currentMutationRate = config.initialMutationRate;
            }

            if (unevolvedGenerations > 0 && unevolvedGenerations % 50 == 0 && currentMutationRate < 0.5) {
                currentMutationRate += 0.05;
            }

            population = nextGen;
        }

        population.sort((a, b) -> Long.compare(b.getFitness(), a.getFitness()));
        return population;
    }
}
