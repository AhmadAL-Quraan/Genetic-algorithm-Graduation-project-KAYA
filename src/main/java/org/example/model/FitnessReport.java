package org.example.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class FitnessReport {
    public int roomConflicts;
    public int instructorConflicts;
    public int studentConflicts;
    public int totalPenalty;
    public HashSet<Class> conflictingClasses;

    public FitnessReport() {
        this.roomConflicts = 0;
        this.instructorConflicts = 0;
        this.studentConflicts = 0;
        this.totalPenalty = 0;
        this.conflictingClasses = new HashSet<>();
    }

    @Override
    public String toString() {
        return "--- FITNESS REPORT ---\n" +
                "Room Conflicts:       " + roomConflicts + " (Penalty: " + (roomConflicts * -10) + ")\n" +
                "Instructor Conflicts: " + instructorConflicts + " (Penalty: " + (instructorConflicts * -10) + ")\n" +
                "Student Year Conflicts: " + studentConflicts + " (Penalty: " + (studentConflicts * -5) + ")\n" +
                "----------------------\n" +
                "TOTAL FITNESS SCORE:  " + totalPenalty + "\n";
    }
}

