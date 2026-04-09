package com.kaya.geneticAlgorithm;

import lombok.Data;

@Data
public class ScheduleRequestDTO {
    private Long populationSize;
    private Long maxGenerations;
    private Double mutationRate;
    private Long tournamentSize;
    private Long elitismCount;
}