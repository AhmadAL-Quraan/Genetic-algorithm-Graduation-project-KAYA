package com.kaya.model;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;

@Getter
@Setter
public class FitnessReport {
    private Long roomConflicts;
    private Long instructorConflicts;
    private Long studentConflicts;
    private Long totalPenalty;
    private HashSet<Lecture> conflictingLectures;

    public FitnessReport() {
        this.roomConflicts = 0L;
        this.instructorConflicts = 0L;
        this.studentConflicts = 0L;
        this.totalPenalty = 0L;
        this.conflictingLectures = new HashSet<>();
    }

    @Override
    public String toString() {
        return "--- FITNESS REPORT ---\n" +
                "Room Conflicts:       " + roomConflicts + " (Penalty: " + (roomConflicts * -100) + ")\n" +
                "Instructor Conflicts: " + instructorConflicts + " (Penalty: " + (instructorConflicts * -100) + ")\n" +
                "Student Year Conflicts: " + studentConflicts + " (Penalty: " + (studentConflicts * -1) + ")\n" +
                "----------------------\n" +
                "TOTAL FITNESS SCORE:  " + totalPenalty + "\n";
    }
}
