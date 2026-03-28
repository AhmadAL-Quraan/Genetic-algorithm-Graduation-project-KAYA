//package solver;
//
//import com.google.ortools.sat.*;
//
//public class TimetableModel {
//
//    // الموديل الرئيسي
//    public CpModel model;
//
//    public BoolVar[][][] x;
//
//    public TimetableModel() {
//
//        // إنشاء الموديل
//        model = new CpModel();
//
//        int C = 3; // Courses
//        int T = 4; // TimeSlots
//        int R = 2; // Rooms
//
//        int[] courseTeacher = {
//                0, // Course 0 → Teacher 0
//                1, // Course 1 → Teacher 1
//                0  // Course 2 → Teacher 0
//        };
//
//        int[] courseGroup = {
//                0, // Course 0 → Group 0
//                0, // Course 1 → Group 0
//                1  // Course 2 → Group 1
//        };
//
//
//        x = new BoolVar[C][T][R];
//        for (int c = 0; c < C; c++) {
//            for (int t = 0; t < T; t++) {
//                for (int r = 0; r < R; r++) {
//                    x[c][t][r] =
//                            model.newBoolVar(
//                                    "c_" + c +
//                                            "_t_" + t +
//                                            "_r_" + r
//                            );
//                }
//            }
//        }
//
//        // add Constraint
//
//        addCourseConstraints(C, T, R);
//
//        addRoomConstraints(C, T, R);
//
//        addTeacherConstraints(
//                C,
//                T,
//                R,
//                courseTeacher
//        );
//
//        addGroupConstraints(
//                C,
//                T,
//                R,
//                courseGroup
//        );
//
//        // Run Solver
//
//        solveModel(C, T, R);
//
//        System.out.println(
//                "Timetable Model Created"
//        );
//    }
//
//    private void addCourseConstraints(
//            int C,
//            int T,
//            int R) {
//
//        for (int c = 0; c < C; c++) {
//
//            // نجمع كل الاحتمالات
//            BoolVar[] vars =
//                    new BoolVar[T * R];
//
//            int index = 0;
//
//            for (int t = 0; t < T; t++) {
//
//                for (int r = 0; r < R; r++) {
//
//                    vars[index++] =
//                            x[c][t][r];
//                }
//            }
//
//            // لازم يحصل مرة واحدة فقط
//            model.addExactlyOne(vars);
//        }
//
//        System.out.println(
//                "Course Constraints Added"
//        );
//    }
//
//    private void addRoomConstraints(
//            int C,
//            int T,
//            int R) {
//
//        for (int t = 0; t < T; t++) {
//
//            for (int r = 0; r < R; r++) {
//
//                // نجمع كل المواد في نفس القاعة ونفس الوقت
//                BoolVar[] vars =
//                        new BoolVar[C];
//
//                for (int c = 0; c < C; c++) {
//
//                    vars[c] =
//                            x[c][t][r];
//                }
//
//                // لا أكثر من مادة واحدة
//                model.addAtMostOne(vars);
//            }
//        }
//
//        System.out.println(
//                "Room Constraints Added"
//        );
//    }
//
//    private void addTeacherConstraints(
//            int C,
//            int T,
//            int R,
//            int[] courseTeacher) {
//
//        // عدد الدكاترة
//        int numTeachers = 2;
//
//        for (int t = 0; t < T; t++) {
//
//            for (int teacher = 0;
//                 teacher < numTeachers;
//                 teacher++) {
//
//                // جمع كل المواد الخاصة بهذا الدكتور
//                java.util.List<BoolVar> vars =
//                        new java.util.ArrayList<>();
//
//                for (int c = 0; c < C; c++) {
//
//                    if (courseTeacher[c]
//                            == teacher) {
//
//                        for (int r = 0; r < R; r++) {
//
//                            vars.add(
//                                    x[c][t][r]
//                            );
//                        }
//                    }
//                }
//
//                if (!vars.isEmpty()) {
//
//                    model.addAtMostOne(
//                            vars.toArray(
//                                    new BoolVar[0]
//                            )
//                    );
//                }
//            }
//        }
//
//        System.out.println(
//                "Teacher Constraints Added"
//        );
//    }
//
//    private void addGroupConstraints(
//            int C,
//            int T,
//            int R,
//            int[] courseGroup) {
//
//        // عدد المجموعات
//        int numGroups = 2;
//
//        for (int t = 0; t < T; t++) {
//
//            for (int group = 0;
//                 group < numGroups;
//                 group++) {
//
//                java.util.List<BoolVar> vars =
//                        new java.util.ArrayList<>();
//
//                for (int c = 0; c < C; c++) {
//
//                    if (courseGroup[c]
//                            == group) {
//
//                        for (int r = 0; r < R; r++) {
//
//                            vars.add(
//                                    x[c][t][r]
//                            );
//                        }
//                    }
//                }
//
//                if (!vars.isEmpty()) {
//
//                    model.addAtMostOne(
//                            vars.toArray(
//                                    new BoolVar[0]
//                            )
//                    );
//                }
//            }
//        }
//
//        System.out.println(
//                "Group Constraints Added"
//        );
//    }
//
//    public void solveModel(
//            int C,
//            int T,
//            int R) {
//
//        CpSolver solver =
//                new CpSolver();
//
//        CpSolverStatus status =
//                solver.solve(model);
//
//        if (status ==
//                CpSolverStatus.FEASIBLE
//                || status ==
//                CpSolverStatus.OPTIMAL) {
//
//            System.out.println(
//                    "Solution Found!"
//            );
//
//            // طباعة النتيجة
//
//            for (int c = 0; c < C; c++) {
//
//                for (int t = 0; t < T; t++) {
//
//                    for (int r = 0; r < R; r++) {
//
//                        if (solver.value(
//                                x[c][t][r]) == 1) {
//
//                            System.out.println(
//                                    "Course " + c
//                                            + " → Time " + t
//                                            + " → Room " + r
//                            );
//                        }
//                    }
//                }
//            }
//
//        } else {
//
//            System.out.println(
//                    "No Solution Found!"
//            );
//        }
//    }
//}