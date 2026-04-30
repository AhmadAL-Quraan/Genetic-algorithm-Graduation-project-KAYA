package com.kaya.model;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * A runtime POJO (Not persisted in DB) that tracks the health/fitness of a specific TimeTable.
 * Acts as the "Scorecard" for the Genetic Algorithm to evaluate how good a schedule is.
 */
@Getter
@Setter
public class FitnessReport {
    private Long roomConflicts;
    private Long instructorConflicts;
    private Long studentConflicts;
    private Long totalPenalty;

    /**
     * CRITICAL ARCHITECTURAL CHOICE: Using a Set (HashSet) instead of a List.
     * If a single Lecture overlaps with 3 other lectures, it will only be added ONCE.
     * This prevents the 'Mutation' process from getting skewed probabilities and
     * keeps the memory footprint minimal during the evolution of thousands of generations.
     */
    private Set<Lecture> conflictingLectures;

    public FitnessReport() {
        this.roomConflicts = 0L;
        this.instructorConflicts = 0L;
        this.studentConflicts = 0L;
        this.totalPenalty = 0L;
        this.conflictingLectures = new HashSet<>();
    }

    @Override
    public String toString() {
        // Formatting the output to dynamically reflect the penalty weights applied in FitnessCalculator.
        return "--- FITNESS REPORT ---\n" +
                "Room Conflicts:       " + roomConflicts + " (Penalty: " + (roomConflicts * -100) + ")\n" +
                "Instructor Conflicts: " + instructorConflicts + " (Penalty: " + (instructorConflicts * -100) + ")\n" +
                "Student Year Conflicts: " + studentConflicts + " (Penalty: " + (studentConflicts * -1) + ")\n" +
                "----------------------\n" +
                "TOTAL FITNESS SCORE:  " + totalPenalty + "\n";
    }
}