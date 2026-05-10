package com.kaya.algorithm;

import com.kaya.model.Lecture;
import com.kaya.model.Room;
import com.kaya.model.TimeSlot;
import com.kaya.model.TimeTable;

import java.time.DayOfWeek;
import java.util.*;

public class FitnessCalculator {

    public static Integer calculateFitness(TimeTable tt) {

        int totalFitness = 0;
        tt.getReport().setTotalPenalty(0);
        tt.getReport().setStudentConflicts(0);
        tt.getReport().setInstructorConflicts(0);
        tt.getReport().setRoomConflicts(0);
        tt.getReport().getConflictingLectures().clear();

        Map<Room, List<Lecture>> roomGroups = new HashMap<>();
        Map<String, List<Lecture>> instructorGroups = new HashMap<>();
        Map<String, List<Lecture>> deptYearGroups = new HashMap<>();

        for (Lecture c : tt.getLectures()) {
            if (c.getRoom() != null) {
                roomGroups.computeIfAbsent(c.getRoom(), k -> new ArrayList<>()).add(c);
            }
            if (c.getInstructor() != null) {
                instructorGroups.computeIfAbsent(c.getInstructor(), k -> new ArrayList<>()).add(c);
            }
            if (c.getCourse() != null && c.getCourse().getCourseNumber() != null
                    && !c.getCourse().getCourseNumber().isEmpty()) {
                String deptYearKey = c.getCourse().getCourseSymbol()
                        + "-" + c.getCourse().getCourseNumber().charAt(0);
                deptYearGroups.computeIfAbsent(deptYearKey, k -> new ArrayList<>()).add(c);
            }
        }

        for (List<Lecture> roomList : roomGroups.values()) {
            totalFitness += checkInternalConflicts(tt, roomList, "Room Conflict", 10);
        }

        for (List<Lecture> instructorList : instructorGroups.values()) {
            totalFitness += checkInternalConflicts(tt, instructorList, "Instructor Conflict", 10);
        }

        for (List<Lecture> deptYearList : deptYearGroups.values()) {
            totalFitness += checkInternalConflicts(tt, deptYearList, "Student Year Conflict", 20);
        }

        tt.getReport().setTotalPenalty(totalFitness);
        return totalFitness;
    }

    private static int checkInternalConflicts(TimeTable tt, List<Lecture> group,
                                               String conflictType, int penalty_weight) {
        int penalty = 0;
        for (int i = 0; i < group.size(); i++) {
            for (int j = i + 1; j < group.size(); j++) {
                Lecture c1 = group.get(i);
                Lecture c2 = group.get(j);

                if (conflictsWith(c1.getTimeSlot(), c2.getTimeSlot())) {
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

    public static boolean conflictsWith(TimeSlot ts1, TimeSlot ts2) {
        if (ts1 == null || ts2 == null) return false;
        boolean dayOverlap = false;
        for (DayOfWeek day : ts1.getDays()) {
            if (ts2.getDays().contains(day)) {
                dayOverlap = true;
                break;
            }
        }
        if (!dayOverlap) return false;
        return ts1.getStartTime().isBefore(ts2.getEndTime())
                && ts2.getStartTime().isBefore(ts1.getEndTime());
    }
}
