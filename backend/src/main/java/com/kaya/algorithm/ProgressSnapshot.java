package com.kaya.algorithm;

public class ProgressSnapshot {
    public final int generation;
    public final int maxGenerations;
    public final int bestFitness;
    public final int roomConflicts;
    public final int instructorConflicts;
    public final int studentConflicts;
    public final double mutationRate;
    public final boolean perfect;

    public ProgressSnapshot(int generation, int maxGenerations, int bestFitness,
                            int roomConflicts, int instructorConflicts, int studentConflicts,
                            double mutationRate, boolean perfect) {
        this.generation = generation;
        this.maxGenerations = maxGenerations;
        this.bestFitness = bestFitness;
        this.roomConflicts = roomConflicts;
        this.instructorConflicts = instructorConflicts;
        this.studentConflicts = studentConflicts;
        this.mutationRate = mutationRate;
        this.perfect = perfect;
    }
}