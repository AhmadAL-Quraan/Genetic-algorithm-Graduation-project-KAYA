package com.kaya.dto.response;

public record GenerationProgress(
        String phase,
        int generation,
        int maxGenerations,
        long bestFitness,
        long roomConflicts,
        long instructorConflicts,
        long studentConflicts,
        double mutationRate
) {
    public static GenerationProgress initializing() {
        return new GenerationProgress("initializing", 0, 0, 0, 0, 0, 0, 0);
    }

    public static GenerationProgress evolving(int gen, int maxGen, long fitness,
                                               long room, long instructor, long student,
                                               double mutRate) {
        return new GenerationProgress("evolving", gen, maxGen, fitness, room, instructor, student, mutRate);
    }

    public static GenerationProgress perfect(int gen, int maxGen) {
        return new GenerationProgress("perfect", gen, maxGen, 0, 0, 0, 0, 0);
    }

    public static GenerationProgress saving() {
        return new GenerationProgress("saving", 0, 0, 0, 0, 0, 0, 0);
    }
}
