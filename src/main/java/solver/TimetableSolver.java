package solver;

import com.google.ortools.sat.*;

import model.Course;
import model.Room;
import model.Teacher;
import model.TimeSlot;

import java.util.List;

public class TimetableSolver {

    private List<Course> courses;
    private List<TimeSlot> slots;
    private List<Teacher> teachers;
    private List<Room> rooms;

    public TimetableSolver(
            List<Course> courses,
            List<TimeSlot> slots,
            List<Teacher> teachers,
            List<Room> rooms) {

        this.courses = courses;
        this.slots = slots;
        this.teachers = teachers;
        this.rooms = rooms;
    }

    public void solve() {

        TimetableModelBuilder builder =
                new TimetableModelBuilder(
                        courses,
                        slots,
                        teachers,
                        rooms
                );

        CpModel model =
                builder.build();

        CpSolver solver =
                new CpSolver();

        solver.getParameters()
                .setMaxTimeInSeconds(120);

        CpSolverStatus status =
                solver.solve(model);

        System.out.println(
                "Solver Status = " + status
        );

        System.out.println(
                "Objective Value = "
                        + solver.objectiveValue()
        );

        if (status == CpSolverStatus.FEASIBLE
                || status == CpSolverStatus.OPTIMAL) {

            SolutionPrinter.print(
                    solver,
                    builder.getX(),
                    builder.getRoomAssign(),
                    courses,
                    slots,
                    rooms
            );
        }
    }
}