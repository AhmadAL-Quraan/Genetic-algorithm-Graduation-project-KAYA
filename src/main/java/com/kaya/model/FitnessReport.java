package com.kaya.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FitnessReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long roomConflicts;
    private Long instructorConflicts;
    private Long studentConflicts;
    private Long totalPenalty;

    // برمجنا على الـ Interface (Set) بدل الـ Implementation (HashSet)
    @OneToMany
    private Set<Lecture> conflictingLectures = new HashSet<>();

    @Override
    public String toString() {
        // ظبطنا الأرقام هنا عشان تطابق أوزان العقوبات اللي عملناها في الـ FitnessCalculator
        return "--- FITNESS REPORT ---\n" +
                "Room Conflicts:       " + roomConflicts + " (Penalty: " + (roomConflicts * -1000) + ")\n" +
                "Instructor Conflicts: " + instructorConflicts + " (Penalty: " + (instructorConflicts * -1000) + ")\n" +
                "Student Year Conflicts: " + studentConflicts + " (Penalty: " + (studentConflicts * -1) + ")\n" +
                "----------------------\n" +
                "TOTAL FITNESS SCORE:  " + totalPenalty + "\n";
    }
}