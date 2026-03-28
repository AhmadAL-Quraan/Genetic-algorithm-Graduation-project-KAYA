package solver.variables;

import com.google.ortools.sat.*;

import model.Course;
import model.Room;
import model.TimeSlot;

import java.util.List;

public class VariableFactory {

    private BoolVar[][] x;
    private BoolVar[][] roomAssign;
    private BoolVar[][][] y;

    public void createVariables(
            CpModel model,
            List<Course> courses,
            List<TimeSlot> slots,
            List<Room> rooms) {

        createCourseSlotVariables(
                model,
                courses,
                slots
        );

        createRoomVariables(
                model,
                courses,
                rooms
        );

        createLinkVariables(
                model,
                courses,
                slots,
                rooms
        );

        System.out.println(
                "Variables Created ✔"
        );
    }

    public BoolVar[][] getX() {
        return x;
    }

    public BoolVar[][] getRoomAssign() {
        return roomAssign;
    }

    public BoolVar[][][] getY() {
        return y;
    }

    private void createCourseSlotVariables(
            CpModel model,
            List<Course> courses,
            List<TimeSlot> slots) {

        x = new BoolVar[
                courses.size()
                ][
                slots.size()
                ];

        for (int c = 0; c < courses.size(); c++) {

            for (int s = 0; s < slots.size(); s++) {

                x[c][s] =
                        model.newBoolVar(
                                "c" + c + "_s" + s
                        );
            }
        }
    }

    private void createRoomVariables(
            CpModel model,
            List<Course> courses,
            List<Room> rooms) {

        roomAssign = new BoolVar[
                courses.size()
                ][
                rooms.size()
                ];

        for (int c = 0; c < courses.size(); c++) {

            for (int r = 0; r < rooms.size(); r++) {

                roomAssign[c][r] =
                        model.newBoolVar(
                                "c" + c + "_r" + r
                        );
            }
        }
    }

    private void createLinkVariables(
            CpModel model,
            List<Course> courses,
            List<TimeSlot> slots,
            List<Room> rooms) {

        y = new BoolVar[
                courses.size()
                ][
                slots.size()
                ][
                rooms.size()
                ];

        for (int c = 0; c < courses.size(); c++) {

            Course course =
                    courses.get(c);

            if (course.getIsOnline())
                continue;

            for (int s = 0; s < slots.size(); s++) {

                for (int r = 0; r < rooms.size(); r++) {

                    y[c][s][r] =
                            model.newBoolVar(
                                    "c"
                                            + c
                                            + "_s"
                                            + s
                                            + "_r"
                                            + r
                            );
                }
            }
        }
    }
}