package com.kaya.model;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class FitnessReport {
    private Long roomConflicts;
    private Long instructorConflicts;
    private Long studentConflicts;
    private Long totalPenalty;

    // برمجنا على الـ Interface (Set) بدل الـ Implementation (HashSet)
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
        // ظبطنا الأرقام هنا عشان تطابق أوزان العقوبات اللي عملناها في الـ FitnessCalculator
        return "--- FITNESS REPORT ---\n" +
                "Room Conflicts:       " + roomConflicts + " (Penalty: " + (roomConflicts * -1000) + ")\n" +
                "Instructor Conflicts: " + instructorConflicts + " (Penalty: " + (instructorConflicts * -1000) + ")\n" +
                "Student Year Conflicts: " + studentConflicts + " (Penalty: " + (studentConflicts * -1) + ")\n" +
                "----------------------\n" +
                "TOTAL FITNESS SCORE:  " + totalPenalty + "\n";
    }
}