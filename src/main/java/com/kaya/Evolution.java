package com.kaya;

import com.kaya.model.*;

import java.util.*;

public class Evolution {
    static int elitismCount;
    static int populationSize;
    static double mutationRate;
    static int tournamentSize;

    // Tournament Selection: Picks the best TimeTable out of tournamentSize random individuals from the timeTables population.
    public static TimeTable tournamentSelection(List<TimeTable> population) {
        Random rand = new Random();
        TimeTable best = null;
        for (int i = 0; i < tournamentSize; i++) {
            TimeTable ind = population.get(rand.nextInt(population.size()));
            if (best == null || ind.fitness > best.fitness) {
                best = ind;
            }
        }
        return best;
    }
    // initializes an array with random TimeTables.
    public static ArrayList<TimeTable> initializePopulation(ArrayList<Lecture> lectures, Map<String, HashSet<TimeSlot>> timePools, Map<String, HashSet<Room>> roomPools) {
        ArrayList<TimeTable> population = new ArrayList<>();
        for (int i = 0; i < populationSize; i++) {
            // Deep copy the initial class list so each schedule is unique
            ArrayList<Lecture> individualClasses = new ArrayList<>();
            for (Lecture lecture : lectures) {
                individualClasses.add(new Lecture(lecture.getId(), lecture.getCourse(), null, null, lecture.getNumber(), lecture.getInstructor()));
            }
            initializeRandomly(timePools, roomPools, individualClasses);
            tt.calculateFitness();
            population.add(tt);
        }
        return population;
    }
    // Generates fitter populations.
    public static ArrayList<TimeTable> evolveGenerations(ArrayList<TimeTable> population, int maxGenerations, Map<String, HashSet<TimeSlot>> timePools, Map<String, HashSet<Room>> roomPools) {
        int unevolvedGenerations = 0;
        for (int gen = 1; gen <= maxGenerations; gen++) {
            // Sort the timetables based on fitness for Elitism (Highest fitness first)
            population.sort((a, b) -> Long.compare(b.fitness, a.fitness));
            System.out.println("Generation " + gen + " | Best Fitness: " + population.get(0).fitness);

            // Check if we found a perfect solution
            if (population.get(0).fitness == 0) {
                System.out.println("--- Perfect Schedule Found! ---");
                break;
            }

            ArrayList<TimeTable> nextGen = new ArrayList<>();

            // ELITISM: Keep the best survivors
            for (int i = 0; i < elitismCount; i++) {
                nextGen.add(population.get(i));
            }

            // REPRODUCTION: Fill up the rest of the generation
            while (nextGen.size() < populationSize) {
                TimeTable p1 = Evolution.tournamentSelection(population);
                TimeTable p2 = Evolution.tournamentSelection(population);

                TimeTable child = TimeTable.crossover(p1, p2);
                child.calculateFitness();

                // MUTATION
                if (Math.random() < mutationRate) {
                    child.mutate(timePools, roomPools);
                }

                child.calculateFitness();
                nextGen.add(child);
            }

            if (population.get(0).fitness >= nextGen.get(0).fitness) {
                unevolvedGenerations++;
            }
            else {
                unevolvedGenerations = 0;
                mutationRate = 0.05;
            }
            if (unevolvedGenerations > 0 && unevolvedGenerations % 50 == 0 && mutationRate < 0.5) {
                mutationRate += 0.05;
            }
            population = nextGen;
        }
        population.sort((a, b) -> Long.compare(b.fitness, a.fitness));
        return population;
    }
    // this function     is not used currently but may be used in future for optimization.
    public static ArrayList<TimeTable> islandsMerge(ArrayList<ArrayList<TimeTable>> islands) {
        Random rand = new Random();
        ArrayList<TimeTable> finalPopulation = new ArrayList<>();

        int min = 1;
        int max = islands.getFirst().size() / 5;
        int index = 0;
        for (ArrayList<TimeTable> island : islands) {
            index = 0;
            int number = rand.nextInt(max - min + 1) + min;
            while (number > 0) {
                finalPopulation.add(island.get(index));
                index++;
                number--;
            }
        }
        while(finalPopulation.size() < islands.getFirst().size())
            finalPopulation.add(islands.get(4).get(index++));
        return finalPopulation;
    }



    public static void initializeRandomly(Map<String, HashSet<TimeSlot>> timePools, Map<String, HashSet<Room>> roomPools, ArrayList<Lecture> individualClasses) {

        ArrayList<Lecture> lectures = individualClasses;
        Long fitness = 0L;
        FitnessReport report = new FitnessReport();

        List<TimeSlot> timePool;
        List<Room> roomPool;


        Random rand = new Random();
        for (Lecture lecture : lectures) {
            timePool = new ArrayList<>(lecture.getTimeSlot(timePools));
            roomPool = new ArrayList<>(lecture.getRoom(roomPools));
            // Randomly pick an allele(or a gene) from the pools
            lecture.setTimeSlot(timePool.get(rand.nextInt(timePool.size())));
            lecture.setRoom(roomPool.get(rand.nextInt(roomPool.size())));
        }
        calculateFitness();
    }
}
