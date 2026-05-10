package com.kaya.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProgressResponse {
    private String phase;
    private int generation;
    private int maxGenerations;
    private int bestFitness;
    private int roomConflicts;
    private int instructorConflicts;
    private int studentConflicts;
    private double mutationRate;
}
