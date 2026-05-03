package com.kaya.algorithm;

import com.kaya.model.Lecture;
import com.kaya.model.Room;
import com.kaya.model.TimeSlot;
import com.kaya.model.TimeTable;
import com.kaya.model.enums.RoomType;
import com.kaya.model.enums.TeachingMethod;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

/**
 * The core Genetic Engine. Upgraded to support Epoch-based evolution
 * for the Island Model Architecture. It is completely Stateless.
 */
public class EvolutionEngineIsland {

    private final GAConfig config;

    public EvolutionEngineIsland(GAConfig config) {
        this.config = config;
    }

    /**
     * Initializes a random population of a specific size for a single island.
     */
    public ArrayList<TimeTable> initializePopulation(ArrayList<Lecture> lectures,
                                                     Map<TeachingMethod, HashSet<TimeSlot>> timePools,
                                                     Map<RoomType, HashSet<Room>> roomPools,
                                                     int size) {
        ArrayList<TimeTable> population = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            ArrayList<Lecture> individualClasses = new ArrayList<>();
            for (Lecture c : lectures) {
                individualClasses.add(new Lecture(c.getId(), c.getCourse(), null, null, c.getSectionNumber(), c.getInstructor()));
            }
            TimeTable timeTable = new TimeTable(individualClasses);
            TimeTableInitializer.initializeRandomly(timeTable, timePools, roomPools);
            population.add(timeTable);
        }
        return population;
    }

    /**
     * Evolves a specific island for a set number of generations (An Epoch).
     * Modifies the island's state in-place. Thread-Safe because each thread
     * operates on a distinct Island object.
     */
    public void evolveIslandEpoch(Island island, int epochsToRun,
                                  Map<TeachingMethod, HashSet<TimeSlot>> timePools,
                                  Map<RoomType, HashSet<Room>> roomPools) {

        ArrayList<TimeTable> population = island.getPopulation();
        int stagnationThreshold = Math.max(1, (int) (config.maxGenerations * config.stagnationToleranceRatio));
        int actualElites = Math.max(1, (int) (population.size() * config.elitismRatio));

        evolutionLoop:
        for (int gen = 1; gen <= epochsToRun; gen++) {

            // 1. Evaluation & Sorting
            population.sort((a, b) -> Long.compare(b.getReport().getTotalPenalty(), a.getReport().getTotalPenalty()));

            if (population.get(0).getReport().getTotalPenalty() == 0) {
                island.setPerfectScheduleFound(true);
                break evolutionLoop;
            }

            ArrayList<TimeTable> nextGen = new ArrayList<>();
            long currentBestFitness = population.get(0).getReport().getTotalPenalty();
            long newGenerationBestFitness = currentBestFitness;

            // 2. ELITISM
            for (int i = 0; i < actualElites; i++) {
                nextGen.add(population.get(i));
            }

            // 3. REPRODUCTION & 4. MUTATION
            while (nextGen.size() < population.size()) {
                TimeTable p1 = Selection.tournamentSelection(population, config.tournamentSize);
                TimeTable p2 = Selection.tournamentSelection(population, config.tournamentSize);

                TimeTable child = GeneticOperators.crossover(p1, p2);
                FitnessCalculator.calculateFitness(child);

                if (child.getReport().getTotalPenalty() == 0) {
                    island.setPerfectScheduleFound(true);
                    nextGen.add(child);
                    population = nextGen;
                    break evolutionLoop;
                }

                if (child.getReport().getTotalPenalty() > newGenerationBestFitness) {
                    newGenerationBestFitness = child.getReport().getTotalPenalty();
                }

                // 4. SMART MUTATION: Fitness-Proportionate Probability
                double individualMutationChance = island.getCurrentMutationRate();
                if (child.getReport().getTotalPenalty() < newGenerationBestFitness) {
                    individualMutationChance = Math.min(0.80, island.getCurrentMutationRate() * 2.0);
                }

                if (Math.random() < individualMutationChance) {
                    GeneticOperators.mutate(child, timePools, roomPools, config.mutationImpactRatio);
                    FitnessCalculator.calculateFitness(child);

                    if (child.getReport().getTotalPenalty() == 0) {
                        island.setPerfectScheduleFound(true);
                        nextGen.add(child);
                        population = nextGen;
                        break evolutionLoop;
                    }
                    if (child.getReport().getTotalPenalty() > newGenerationBestFitness) {
                        newGenerationBestFitness = child.getReport().getTotalPenalty();
                    }
                }
                nextGen.add(child);
            }

            // 5. ADAPTIVE MUTATION LOGIC (Island-Specific)
            if (currentBestFitness >= newGenerationBestFitness) {
                island.setUnevolvedGenerations(island.getUnevolvedGenerations() + 1);
            } else {
                island.setUnevolvedGenerations(0);
                island.setCurrentMutationRate(config.initialMutationRate);
            }

            if (island.getUnevolvedGenerations() > 0 &&
                    (island.getUnevolvedGenerations() % stagnationThreshold) == 0 &&
                    island.getCurrentMutationRate() < 0.5) {
                island.setCurrentMutationRate(island.getCurrentMutationRate() + 0.05);
            }

            population = nextGen;
        }

        // Final sort at the end of the epoch
        population.sort((a, b) -> Long.compare(b.getReport().getTotalPenalty(), a.getReport().getTotalPenalty()));
        island.setPopulation(population);
    }
}