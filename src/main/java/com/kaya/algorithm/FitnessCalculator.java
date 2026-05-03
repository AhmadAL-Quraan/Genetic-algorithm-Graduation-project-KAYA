package com.kaya.algorithm;

import com.kaya.model.Lecture;
import com.kaya.model.Room;
import com.kaya.model.TimeSlot;
import com.kaya.model.TimeTable;

import java.util.*;

/**
 * Evaluates the quality of a given TimeTable (Chromosome).
 * Calculates the fitness score by applying weighted penalties for hard and soft constraint violations.
 * A flawless schedule will result in a fitness score of 0.
 */
public class FitnessCalculator {

    // Defined penalty weights to differentiate between severe and minor conflicts.
    private static final int HARD_CONFLICT_PENALTY = 100; // Unforgivable: Room or Instructor double-booking
    private static final int SOFT_CONFLICT_PENALTY = 5;   // Minor: Same cohort students having simultaneous classes

    /**
     * Calculates the total fitness score of a schedule by grouping lectures and checking for overlaps.
     *
     * @param tt The TimeTable to be evaluated.
     * @return The calculated total fitness penalty (negative value, closer to 0 is better).
     */
    public static Long calculateFitness(TimeTable tt) {
        Long totalFitness = 0L;

        // Reset the report metrics for a fresh evaluation
        tt.getReport().setTotalPenalty(0L);
        tt.getReport().setStudentConflicts(0L);
        tt.getReport().setInstructorConflicts(0L);
        tt.getReport().setRoomConflicts(0L);

        // Clear the old conflicting lectures to free up memory and prevent inaccurate mutation targeting
        tt.getReport().getConflictingLectures().clear();

        Map<Room, List<Lecture>> roomGroups = new HashMap<>();
        Map<String, List<Lecture>> instructorGroups = new HashMap<>();
        Map<String, List<Lecture>> deptYearGroups = new HashMap<>();

        for (Lecture c : tt.getLectures()) {
            roomGroups.computeIfAbsent(c.getRoom(), k -> new ArrayList<>()).add(c);
            instructorGroups.computeIfAbsent(c.getInstructor(), k -> new ArrayList<>()).add(c);

            // Create a unique key (e.g., "CS-1") to group students by department and year
            String deptYearKey = c.getCourse().getCourseSymbol() + "-" + c.getCourse().getCourseNumber().charAt(0);
            deptYearGroups.computeIfAbsent(deptYearKey, k -> new ArrayList<>()).add(c);
        }

        // 1. Hard Conflicts (Room Constraints)
        for (List<Lecture> roomList : roomGroups.values()) {
            totalFitness += checkInternalConflicts(tt, roomList, "Room Conflict", HARD_CONFLICT_PENALTY);
        }

        // 2. Hard Conflicts (Instructor Constraints)
        for (List<Lecture> instructorList : instructorGroups.values()) {
            totalFitness += checkInternalConflicts(tt, instructorList, "Instructor Conflict", HARD_CONFLICT_PENALTY);
        }

        // 3. Soft Conflicts (Student Cohort Constraints)
        for (List<Lecture> deptYearList : deptYearGroups.values()) {
            totalFitness += checkInternalConflicts(tt, deptYearList, "Student Year Conflict", SOFT_CONFLICT_PENALTY);
        }

        tt.getReport().setTotalPenalty(totalFitness);
        return totalFitness;
    }

    /**
     * Iterates through a grouped list of lectures to detect and penalize time overlaps.
     *
     * @param tt             The TimeTable being evaluated.
     * @param group          The grouped list of lectures (by room, instructor, or cohort).
     * @param conflictType   The type of conflict being evaluated for reporting purposes.
     * @param penalty_weight The severity weight of the conflict to be subtracted.
     * @return The accumulated penalty for this specific group.
     */
    private static int checkInternalConflicts(TimeTable tt, List<Lecture> group, String conflictType, int penalty_weight) {
        int penalty = 0;

        // Pairwise comparison to find overlapping time slots within the same group
        for (int i = 0; i < group.size(); i++) {
            for (int j = i + 1; j < group.size(); j++) {
                Lecture c1 = group.get(i);
                Lecture c2 = group.get(j);

                // Check for temporal intersection using the external data extractor utility
                if (conflictsWith(c1.getTimeSlot(), c2.getTimeSlot())) {

                    // Log the conflicting classes so the Mutation operator knows what to target
                    tt.getReport().getConflictingLectures().add(c1);
                    tt.getReport().getConflictingLectures().add(c2);

                    penalty -= penalty_weight;
                    tt.getReport().setTotalPenalty(tt.getReport().getTotalPenalty() - penalty_weight);

                    // Increment the appropriate metric counter for the final output report
                    if (conflictType.equals("Room Conflict")) {
                        tt.getReport().setRoomConflicts(tt.getReport().getRoomConflicts() + 1L);
                    } else if (conflictType.equals("Instructor Conflict")) {
                        tt.getReport().setInstructorConflicts(tt.getReport().getInstructorConflicts() + 1L);
                    } else if (conflictType.equals("Student Year Conflict")) {
                        tt.getReport().setStudentConflicts(tt.getReport().getStudentConflicts() + 1L);
                    }
                }
            }
        }
        return penalty;
    }

    /**
     * Evaluates whether two TimeSlots temporally overlap.
     * Strictly utilizes getter methods to respect data encapsulation.
     *
     * @param self  The primary TimeSlot being evaluated.
     * @param other The secondary TimeSlot to compare against.
     * @return true if there is an intersection in both days and active hours, false otherwise.
     */
    public static boolean conflictsWith(TimeSlot self, TimeSlot other) {

        // Collections.disjoint returns true if the sets have no shared elements (days).
        // We negate it (!) to proceed with the time intersection check ONLY if they share at least one day.
        if (!Collections.disjoint(self.getDays(), other.getDays())) {

            // Check for strict temporal overlap: (StartA < EndB) AND (StartB < EndA)
            return self.getStartTime().isBefore(other.getEndTime()) &&
                    other.getStartTime().isBefore(self.getEndTime());
        }
        return false;
    }
}