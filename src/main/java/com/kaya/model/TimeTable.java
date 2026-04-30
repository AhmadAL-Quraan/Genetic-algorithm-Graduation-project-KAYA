package com.kaya.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Represents a single 'Chromosome' (Individual Solution) in the Genetic Algorithm population.
 * Contains a complete schedule of lectures and its evaluated fitness score.
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TimeTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Encapsulated to prevent accidental modification outside the FitnessCalculator.
    // Represents the penalty score (0 is a perfect conflict-free schedule).
    private Long fitness;

    // A complete set of genes (Lectures with assigned Rooms and TimeSlots).
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "timetable_id")
    private List<Lecture> lectures;

    // Marked as @Transient because the report is a runtime calculation used strictly
    // by the Genetic Algorithm (Mutation and Fitness Evaluation). It does not need persistence.
    @Transient
    private FitnessReport report;

    public TimeTable(List<Lecture> lectures) {
        this.fitness = 0L;
        this.lectures = lectures;
        this.report = new FitnessReport();
    }

    @Override
    public String toString() {
        StringBuilder schedule = new StringBuilder();
        for (Lecture lecture : lectures) {
            schedule.append(lecture).append("\n");
        }
        return schedule.toString();
    }
}