package solver.constraints;

import com.google.ortools.sat.*;

import model.Course;
import model.Room;
import model.Teacher;
import model.TimeSlot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SoftConstraints {

    private CpModel model;

    private List<Course> courses;
    private List<TimeSlot> slots;
    private List<Teacher> teachers;
    private List<Room> rooms;

    private BoolVar[][] x;
    private BoolVar[][] roomAssign;

    private List<IntVar> allPenalties;

    public SoftConstraints(
            CpModel model,
            List<Course> courses,
            List<TimeSlot> slots,
            List<Teacher> teachers,
            List<Room> rooms,
            BoolVar[][] x,
            BoolVar[][] roomAssign,
            List<IntVar> allPenalties) {

        this.model = model;
        this.courses = courses;
        this.slots = slots;
        this.teachers = teachers;
        this.rooms = rooms;
        this.x = x;
        this.roomAssign = roomAssign;
        this.allPenalties = allPenalties;
    }

    public void applyAll() {

        addBalancedSections();

        addTeacherCompact();

        addLabPreference();

        addStudentLevelSoftConstraint();

        System.out.println(
                "Soft Constraints Applied ✔"
        );
    }

    // =====================================================

    private void addBalancedSections() {

        HashMap<String, List<Integer>> map =
                new HashMap<>();

        for (int c = 0;
             c < courses.size();
             c++) {

            Course course =
                    courses.get(c);

            String key =
                    course.getDepartment()
                            + "_"
                            + course.getCourseNumber();

            map
                    .computeIfAbsent(
                            key,
                            k -> new ArrayList<>()
                    )
                    .add(c);
        }

        List<IntVar> penalties =
                new ArrayList<>();

        for (String key : map.keySet()) {

            List<Integer> group =
                    map.get(key);

            if (group.size() <= 1)
                continue;

            for (int s = 0;
                 s < slots.size();
                 s++) {

                List<Literal> usage =
                        new ArrayList<>();

                for (int cIndex : group) {

                    usage.add(
                            x[cIndex][s]
                    );
                }

                IntVar load =
                        model.newIntVar(
                                0,
                                group.size(),
                                "load_" + key + "_s" + s
                        );

                model.addEquality(
                        load,
                        LinearExpr.sum(
                                usage.toArray(
                                        new Literal[0]
                                )
                        )
                );

                IntVar overload =
                        model.newIntVar(
                                0,
                                group.size(),
                                "over_" + key + "_s" + s
                        );

                model.addGreaterOrEqual(
                        load,
                        overload
                );

                penalties.add(overload);
            }
        }

        allPenalties.addAll(
                penalties
        );

        System.out.println(
                "Balanced Section Penalties = "
                        + penalties.size()
        );
    }

    private void addTeacherCompact() {

        List<IntVar> penalties =
                new ArrayList<>();

        for (Teacher t : teachers) {

            for (int s = 0;
                 s < slots.size();
                 s++) {

                List<Literal> teacherCourses =
                        new ArrayList<>();

                for (int c = 0;
                     c < courses.size();
                     c++) {

                    if (courses.get(c)
                            .getTeacherId() == t.getId()) {

                        teacherCourses.add(
                                x[c][s]
                        );
                    }
                }

                if (teacherCourses.isEmpty())
                    continue;

                IntVar usedSlot =
                        model.newIntVar(
                                0,
                                1,
                                "teacher_"
                                        + t.getId()
                                        + "_uses_s"
                                        + s
                        );

                model.addGreaterOrEqual(
                        LinearExpr.sum(
                                teacherCourses.toArray(
                                        new Literal[0]
                                )
                        ),
                        usedSlot
                );

                penalties.add(
                        usedSlot
                );
            }
        }

        allPenalties.addAll(
                penalties
        );

        System.out.println(
                "Teacher Compact Penalties = "
                        + penalties.size()
        );
    }

    private void addLabPreference() {

        List<IntVar> penalties =
                new ArrayList<>();

        for (int c = 0;
             c < courses.size();
             c++) {

            Course course =
                    courses.get(c);

            if (!course.getIsRequiresLab())
                continue;

            for (int r = 0;
                 r < rooms.size();
                 r++) {

                if (!rooms.get(r).getIsLab()) {

                    penalties.add(
                            roomAssign[c][r]
                    );
                }
            }
        }

        allPenalties.addAll(
                penalties
        );

        System.out.println(
                "Lab Penalties = "
                        + penalties.size()
        );
    }

    private void addStudentLevelSoftConstraint() {

        HashMap<Integer, List<Integer>> levelMap =
                new HashMap<>();

        for (int c = 0;
             c < courses.size();
             c++) {

            Course course =
                    courses.get(c);

            int level =
                    (int)course.getCourseNumber().charAt(0);

            levelMap
                    .computeIfAbsent(
                            level,
                            k -> new ArrayList<>()
                    )
                    .add(c);
        }

        List<IntVar> penalties =
                new ArrayList<>();

        for (Integer level : levelMap.keySet()) {

            List<Integer> group =
                    levelMap.get(level);

            if (group.size() <= 1)
                continue;

            for (int s = 0;
                 s < slots.size();
                 s++) {

                List<Literal> vars =
                        new ArrayList<>();

                for (int cIndex : group) {

                    vars.add(
                            x[cIndex][s]
                    );
                }

                // load

                IntVar load =
                        model.newIntVar(
                                0,
                                group.size(),
                                "level_load_"
                                        + level
                                        + "_s"
                                        + s
                        );

                model.addEquality(
                        load,
                        LinearExpr.sum(
                                vars.toArray(
                                        new Literal[0]
                                )
                        )
                );

                // overflow

                IntVar overflow =
                        model.newIntVar(
                                0,
                                group.size(),
                                "level_overflow_"
                                        + level
                                        + "_s"
                                        + s
                        );

                // overflow + 1 ≥ load

                model.addGreaterOrEqual(
                        LinearExpr.sum(
                                new IntVar[]{overflow}
                        ),
                        LinearExpr.term(load, 1)
                );

                penalties.add(overflow);
            }
        }

        allPenalties.addAll(
                penalties
        );

        System.out.println(
                "Student Level Groups = "
                        + levelMap.size()
        );

        System.out.println(
                "Student Level Penalties = "
                        + penalties.size()
        );

        System.out.println(
                "Soft Constraint: Student Level Conflict ✔"
        );
    }
}


