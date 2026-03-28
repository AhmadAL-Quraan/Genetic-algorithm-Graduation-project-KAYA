package solver;

import com.google.ortools.sat.*;

import model.Course;
import model.Room;
import model.Teacher;
import model.TimeSlot;
import solver.variables.VariableFactory;
import solver.constraints.*;
import solver.objective.ObjectiveCollector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TimetableModelBuilder {

    private List<Course> courses;
    private List<TimeSlot> slots;
    private List<Teacher> teachers;
    private List<Room> rooms;

    public BoolVar[][] x;
    public BoolVar[][] roomAssign;
    public BoolVar[][][] y;

    private List<IntVar> allPenalties =
            new ArrayList<>();

    private VariableFactory variableFactory;

    private CpModel model;

    public TimetableModelBuilder(
            List<Course> courses,
            List<TimeSlot> slots,
            List<Teacher> teachers,
            List<Room> rooms) {

        this.courses = courses;
        this.slots = slots;
        this.teachers = teachers;
        this.rooms = rooms;

        this.variableFactory =
                new VariableFactory();

        this.model = new CpModel();
    }

    public CpModel build() {

        sortSlotsByTime();

        variableFactory.createVariables(
                model,
                courses,
                slots,
                rooms
        );

        x = variableFactory.getX();
        roomAssign = variableFactory.getRoomAssign();
        y = variableFactory.getY();

        HardConstraints hardConstraints =
                new HardConstraints(
                        model,
                        courses,
                        slots,
                        teachers,
                        rooms,
                        x,
                        roomAssign,
                        y
                );

        hardConstraints.applyAll();

        SoftConstraints softConstraints =
                new SoftConstraints(
                        model,
                        courses,
                        slots,
                        teachers,
                        rooms,
                        x,
                        roomAssign,
                        allPenalties
                );

        softConstraints.applyAll();

        // FINAL OBJECTIVE
        ObjectiveCollector objectiveCollector =
                new ObjectiveCollector(
                        model,
                        allPenalties
                );

        objectiveCollector.buildObjective();
        return model;
    }

    public BoolVar[][] getRoomAssign() {
        return roomAssign;
    }

    public BoolVar[][][] getY() {
        return y;
    }

    public CpModel getModel() {
        return model;
    }

    private void sortSlotsByTime() {

        slots.sort((s1, s2) -> {

            if (s1.getPatternId() != s2.getPatternId())
                return Integer.compare(
                        s1.getPatternId(),
                        s2.getPatternId()
                );

            return Integer.compare(
                    s1.getStartMinutes(),
                    s2.getStartMinutes()
            );
        });

        System.out.println(
                "Slots Sorted By Time ✔"
        );
    }

    public BoolVar[][] getX() {
        return x;
    }
}