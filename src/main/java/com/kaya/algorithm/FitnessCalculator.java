package com.kaya.algorithm;

import com.kaya.model.Lecture;
import com.kaya.model.Room;
import com.kaya.model.TimeSlot;
import com.kaya.model.TimeTable;

import java.util.*;

public class FitnessCalculator {

    // [جديد]: تحديد أوزان العقوبات (Penalty Weights) بشكل احترافي ومدروس
    private static final int HARD_CONFLICT_PENALTY = 100; // جريمة لا تغتفر (تعارض قاعة أو دكتور)
    private static final int SOFT_CONFLICT_PENALTY = 1;    // مشكلة بسيطة (تعارض طلاب نفس السنة)

    public static Long calculateFitness(TimeTable tt) {

        // ======================= الكود القديم =======================
        Long totalFitness = 0L;
        tt.getReport().setTotalPenalty(0L);
        tt.getReport().setStudentConflicts(0L);
        tt.getReport().setInstructorConflicts(0L);
        tt.getReport().setRoomConflicts(0L);
        // يفضل تصفية لستة التعارضات القديمة قبل الحساب الجديد
        tt.getReport().getConflictingLectures().clear();

        Map<Room, List<Lecture>> roomGroups = new HashMap<>();
        Map<String, List<Lecture>> instructorGroups = new HashMap<>();
        // Grouping by Department + Year Level
        Map<String, List<Lecture>> deptYearGroups = new HashMap<>();

        for (Lecture c : tt.getLectures()) {
            roomGroups.computeIfAbsent(c.getRoom(), k -> new ArrayList<>()).add(c);
            instructorGroups.computeIfAbsent(c.getInstructor(), k -> new ArrayList<>()).add(c);

            // Create a unique key like "BIT-2"
            String deptYearKey = c.getCourse().getCourseSymbol() + "-" + c.getCourse().getCourseNumber().charAt(0);
            deptYearGroups.computeIfAbsent(deptYearKey, k -> new ArrayList<>()).add(c);
        }

        // 1. Hard Conflicts (Room)
        for (List<Lecture> roomList : roomGroups.values()) {
            totalFitness += checkInternalConflicts(tt, roomList, "Room Conflict", 10);
        }

        // 2. Hard Conflicts (Instructor)
        for (List<Lecture> instructorList : instructorGroups.values()) {
            totalFitness += checkInternalConflicts(tt, instructorList, "Instructor Conflict", 10);
        }

        // 3. Soft Conflicts (Same Year/Dept Students)
        for (List<Lecture> deptYearList : deptYearGroups.values()) {
            // Use a smaller penalty (e.g., -20) since this is a soft constraint
            totalFitness += checkInternalConflicts(tt, deptYearList, "Student Year Conflict", 5);
        }
        tt.getReport().setTotalPenalty(totalFitness);
        return totalFitness;
        // ============================================================


        /*
        // ======================= الكود الجديد =======================
        Long totalFitness = 0L;
        tt.report.setTotalPenalty(0L);
        tt.report.setStudentConflicts(0L);
        tt.report.setInstructorConflicts(0L);
        tt.report.setRoomConflicts(0L);

        // يفضل تصفية لستة التعارضات القديمة قبل الحساب الجديد عشان ننظف الميموري
        tt.report.getConflictingLectures().clear();

        Map<Room, List<Lecture>> roomGroups = new HashMap<>();
        Map<String, List<Lecture>> instructorGroups = new HashMap<>();
        // Grouping by Department + Year Level
        Map<String, List<Lecture>> deptYearGroups = new HashMap<>();

        for (Lecture c : tt.lectures) {
            roomGroups.computeIfAbsent(c.getRoom(), k -> new ArrayList<>()).add(c);
            instructorGroups.computeIfAbsent(c.getInstructor(), k -> new ArrayList<>()).add(c);

            // Create a unique key like "BIT-2"
            String deptYearKey = c.getCourse().getCourseSymbol() + "-" + c.getCourse().getCourseNumber().charAt(0);
            deptYearGroups.computeIfAbsent(deptYearKey, k -> new ArrayList<>()).add(c);
        }

        // 1. Hard Conflicts (Room) - تمرير العقوبة القاسية
        for (List<Lecture> roomList : roomGroups.values()) {
            totalFitness += checkInternalConflicts(tt, roomList, "Room Conflict", HARD_CONFLICT_PENALTY);
        }

        // 2. Hard Conflicts (Instructor) - تمرير العقوبة القاسية
        for (List<Lecture> instructorList : instructorGroups.values()) {
            totalFitness += checkInternalConflicts(tt, instructorList, "Instructor Conflict", HARD_CONFLICT_PENALTY);
        }

        // 3. Soft Conflicts (Same Year/Dept Students) - تمرير العقوبة الخفيفة
        for (List<Lecture> deptYearList : deptYearGroups.values()) {
            totalFitness += checkInternalConflicts(tt, deptYearList, "Student Year Conflict", SOFT_CONFLICT_PENALTY);
        }

        tt.setFitness(totalFitness);
        return totalFitness;
        */
    }

    // التحقق من التعارض (بقينا نستخدم getDays و getStartTime عشان المتغيرات بقت private)
    public static boolean conflictsWith(TimeSlot self, TimeSlot other) {
        // Collections.disjoint بترجع True لو مفيش أيام مشتركة
        // فبنعكسها (!) عشان لو في أيام مشتركة، نفحص تقاطع الأوقات
        if (!Collections.disjoint(self.getDays(), other.getDays())) {
            return self.getStartTime().isBefore(other.getEndTime()) &&
                    other.getStartTime().isBefore(self.getEndTime());
        }
        return false;
    }

    private static int checkInternalConflicts(TimeTable tt, List<Lecture> group, String conflictType, int penalty_weight) {

        // ======================= الكود القديم =======================
        int penalty = 0;
        // Only compare items within this specific small group
        for (int i = 0; i < group.size(); i++) {
            for (int j = i + 1; j < group.size(); j++) {
                Lecture c1 = group.get(i);
                Lecture c2 = group.get(j);

                if (conflictsWith(c1.getTimeSlot(),c2.getTimeSlot())) {
                    tt.getReport().getConflictingLectures().add(c1);
                    tt.getReport().getConflictingLectures().add(c2);
                    penalty -= penalty_weight;
                    tt.getReport().setTotalPenalty( tt.getReport().getTotalPenalty() - penalty_weight );

                    if (conflictType.equals("Room Conflict")) {
                        tt.getReport().setRoomConflicts( tt.getReport().getRoomConflicts() + 1 );
                    } else if (conflictType.equals("Instructor Conflict")) {
                        tt.getReport().setInstructorConflicts( tt.getReport().getInstructorConflicts() + 1 );
                    } else if (conflictType.equals("Student Year Conflict")) {
                        tt.getReport().setStudentConflicts( tt.getReport().getStudentConflicts() + 1 );
                    }
                }
            }
        }
        return penalty;
        // ============================================================


        /*
        // ======================= الكود الجديد =======================
        int penalty = 0;
        // بنقارن كل محاضرتين ببعض جوه نفس الجروب للتأكد من مفيش أي تقاطع
        for (int i = 0; i < group.size(); i++) {
            for (int j = i + 1; j < group.size(); j++) {
                Lecture c1 = group.get(i);
                Lecture c2 = group.get(j);

                // التأكد من وجود تعارض في الوقت
                if (ExcelDataExtractor.conflictsWith(c1.getTimeSlot(), c2.getTimeSlot())) {

                    // نضيف الكورسات المتعارضة للستة عشان الـ Mutation في الكلاس التاني يمسكها ويصلحها
                    tt.report.getConflictingLectures().add(c1);
                    tt.report.getConflictingLectures().add(c2);

                    penalty -= penalty_weight; // بنطرح قيمة العقوبة المحددة
                    tt.report.setTotalPenalty( tt.report.getTotalPenalty() - penalty_weight );

                    // بنزود العدادات عشان التقرير النهائي
                    if (conflictType.equals("Room Conflict")) {
                        tt.report.setRoomConflicts( tt.report.getRoomConflicts() + 1L );
                    } else if (conflictType.equals("Instructor Conflict")) {
                        tt.report.setInstructorConflicts( tt.report.getInstructorConflicts() + 1L );
                    } else if (conflictType.equals("Student Year Conflict")) {
                        tt.report.setStudentConflicts( tt.report.getStudentConflicts() + 1L );
                    }
                }
            }
        }
        return penalty;

         */
    }
}








