package com.kaya.algorithm;

import com.kaya.algorithm.data.ExcelDataExtractor;
import com.kaya.model.Lecture;
import com.kaya.model.Room;
import com.kaya.model.TimeTable;

import java.util.*;

public class FitnessCalculator {

    private static final int HARD_CONFLICT_PENALTY = 100;
    private static final int SOFT_CONFLICT_PENALTY = 1;

    public static Long calculateFitness(TimeTable tt) {
        Long totalFitness = 0L;
        tt.getReport().setTotalPenalty(0L);
        tt.getReport().setStudentConflicts(0L);
        tt.getReport().setInstructorConflicts(0L);
        tt.getReport().setRoomConflicts(0L);
        tt.getReport().getConflictingLectures().clear();

        Map<Room, List<Lecture>> roomGroups = new HashMap<>();
        Map<String, List<Lecture>> instructorGroups = new HashMap<>();
        Map<String, List<Lecture>> deptYearGroups = new HashMap<>();

        for (Lecture c : tt.getLectures()) {
            roomGroups.computeIfAbsent(c.getRoom(), k -> new ArrayList<>()).add(c);
            instructorGroups.computeIfAbsent(c.getInstructor(), k -> new ArrayList<>()).add(c);

            String deptYearKey = c.getCourse().getCourseSymbol() + "-" + c.getCourse().getCourseNumber().charAt(0);
            deptYearGroups.computeIfAbsent(deptYearKey, k -> new ArrayList<>()).add(c);
        }

        // 1. Hard Conflicts (Room)
        for (List<Lecture> roomList : roomGroups.values()) {
            totalFitness += checkInternalConflicts(tt, roomList, "Room Conflict", HARD_CONFLICT_PENALTY);
        }

        // 2. Hard Conflicts (Instructor)
        for (List<Lecture> instructorList : instructorGroups.values()) {
            totalFitness += checkInternalConflicts(tt, instructorList, "Instructor Conflict", HARD_CONFLICT_PENALTY);
        }

        // 3. Soft Conflicts (Same Year/Dept Students)
        for (List<Lecture> deptYearList : deptYearGroups.values()) {
            totalFitness += checkInternalConflicts(tt, deptYearList, "Student Year Conflict", SOFT_CONFLICT_PENALTY);
        }

        tt.setFitness(totalFitness);
        return totalFitness;
    }

    private static int checkInternalConflicts(TimeTable tt, List<Lecture> group, String conflictType, int penalty_weight) {
        int penalty = 0;
        for (int i = 0; i < group.size(); i++) {
            for (int j = i + 1; j < group.size(); j++) {
                Lecture c1 = group.get(i);
                Lecture c2 = group.get(j);

                if (ExcelDataExtractor.conflictsWith(c1.getTimeSlot(), c2.getTimeSlot())) {
                    tt.getReport().getConflictingLectures().add(c1);
                    tt.getReport().getConflictingLectures().add(c2);
                    penalty -= penalty_weight;
                    tt.getReport().setTotalPenalty(tt.getReport().getTotalPenalty() - penalty_weight);

                    if (conflictType.equals("Room Conflict")) {
                        tt.getReport().setRoomConflicts(tt.getReport().getRoomConflicts() + 1);
                    } else if (conflictType.equals("Instructor Conflict")) {
                        tt.getReport().setInstructorConflicts(tt.getReport().getInstructorConflicts() + 1);
                    } else if (conflictType.equals("Student Year Conflict")) {
                        tt.getReport().setStudentConflicts(tt.getReport().getStudentConflicts() + 1);
                    }
                }
            }
        }
        return penalty;
    }
}
