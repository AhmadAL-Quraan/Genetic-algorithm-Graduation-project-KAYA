package solver.constraints;

import com.google.ortools.sat.*;

import model.Course;
import model.Room;
import model.Teacher;
import model.TimeSlot;

import java.util.ArrayList;
import java.util.List;

public class HardConstraints {

    private CpModel model;

    private List<Course> courses;
    private List<TimeSlot> slots;
    private List<Teacher> teachers;
    private List<Room> rooms;

    private BoolVar[][] x;
    private BoolVar[][] roomAssign;
    private BoolVar[][][] y;

    public HardConstraints(
            CpModel model,
            List<Course> courses,
            List<TimeSlot> slots,
            List<Teacher> teachers,
            List<Room> rooms,
            BoolVar[][] x,
            BoolVar[][] roomAssign,
            BoolVar[][][] y) {

        this.model = model;
        this.courses = courses;
        this.slots = slots;
        this.teachers = teachers;
        this.rooms = rooms;
        this.x = x;
        this.roomAssign = roomAssign;
        this.y = y;
    }

    public void applyAll() {

        addPatternMatchingConstraint();

        addCourseSlotConstraint();

        addTeacherConflictConstraint();

        addRoomLinkConstraint();

        addRoomConflictConstraint();

        addOnlineSlotConstraint();

        addRoomAssignmentConstraint();

        addOnlineNoRoomConstraint();

        System.out.println(
                "Hard Constraints Applied ✔"
        );
    }

    // =====================================================

    private void addPatternMatchingConstraint() {

        for (int c = 0; c < courses.size(); c++) {

            Course course =
                    courses.get(c);

            for (int s = 0; s < slots.size(); s++) {

                TimeSlot slot =
                        slots.get(s);

                if (course.getPatternId()
                        != slot.getPatternId()) {

                    model.addEquality(
                            x[c][s],
                            0
                    );
                }
            }
        }
    }

    private void addCourseSlotConstraint() {

        for (int c = 0; c < courses.size(); c++) {

            BoolVar[] arr =
                    new BoolVar[
                            slots.size()
                            ];

            for (int s = 0; s < slots.size(); s++) {

                arr[s] = x[c][s];
            }

            model.addExactlyOne(arr);
        }
    }

    private void addTeacherConflictConstraint() {

        for (Teacher t : teachers) {

            for (int s = 0; s < slots.size(); s++) {

                List<Literal> list =
                        new ArrayList<>();

                for (int c = 0; c < courses.size(); c++) {

                    if (courses.get(c)
                            .getTeacherId() == t.getId()) {

                        list.add(
                                x[c][s]
                        );
                    }
                }

                if (!list.isEmpty()) {

                    model.addAtMostOne(
                            list.toArray(
                                    new Literal[0]
                            )
                    );
                }
            }
        }
    }

    private void addRoomLinkConstraint() {

        for (int c = 0; c < courses.size(); c++) {

            Course course =
                    courses.get(c);

            if (course.getIsOnline())
                continue;

            for (int s = 0; s < slots.size(); s++) {

                for (int r = 0; r < rooms.size(); r++) {

                    model.addLessOrEqual(
                            y[c][s][r],
                            x[c][s]
                    );

                    model.addLessOrEqual(
                            y[c][s][r],
                            roomAssign[c][r]
                    );
                }
            }
        }
    }

    private void addRoomConflictConstraint() {

        for (int r = 0; r < rooms.size(); r++) {

            for (int s = 0; s < slots.size(); s++) {

                List<Literal> list =
                        new ArrayList<>();

                for (int c = 0; c < courses.size(); c++) {

                    if (courses.get(c).getIsOnline())
                        continue;

                    list.add(
                            y[c][s][r]
                    );
                }

                model.addAtMostOne(
                        list.toArray(
                                new Literal[0]
                        )
                );
            }
        }
    }

    private void addOnlineSlotConstraint() {

        for (int c = 0; c < courses.size(); c++) {

            Course course =
                    courses.get(c);

            for (int s = 0; s < slots.size(); s++) {

                TimeSlot slot =
                        slots.get(s);

                if (course.getIsOnline()
                        && !slot.getIsOnlineSlot()) {

                    model.addEquality(
                            x[c][s],
                            0
                    );
                }

                if (!course.getIsOnline()
                        && slot.getIsOnlineSlot()) {

                    model.addEquality(
                            x[c][s],
                            0
                    );
                }
            }
        }
    }

    private void addRoomAssignmentConstraint() {

        for (int c = 0; c < courses.size(); c++) {

            if (courses.get(c).getIsOnline())
                continue;

            List<Literal> list =
                    new ArrayList<>();

            for (int r = 0; r < rooms.size(); r++) {

                list.add(
                        roomAssign[c][r]
                );
            }

            model.addExactlyOne(
                    list.toArray(
                            new Literal[0]
                    )
            );
        }
    }

    private void addOnlineNoRoomConstraint() {

        for (int c = 0; c < courses.size(); c++) {

            if (!courses.get(c).getIsOnline())
                continue;

            for (int r = 0; r < rooms.size(); r++) {

                model.addEquality(
                        roomAssign[c][r],
                        0
                );
            }
        }
    }
}




