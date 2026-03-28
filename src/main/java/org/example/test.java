package org.example;
import com.google.ortools.Loader;
import com.google.ortools.sat.*;

public class test {

    public static void main(String[] args) {

        Loader.loadNativeLibraries();

        CpModel model = new CpModel();

        // Variables
        IntVar x = model.newIntVar(0, 10, "x");
        IntVar y = model.newIntVar(0, 10, "y");

        // Constraint
        model.addLessOrEqual(
                LinearExpr.sum(new IntVar[]{x, y}),
                10
        );

        // Objective
        model.maximize(
                LinearExpr.weightedSum(
                        new IntVar[]{x, y},
                        new long[]{3, 4}
                )
        );

        // Solve
        CpSolver solver = new CpSolver();

        CpSolverStatus status = solver.solve(model);

        if (status == CpSolverStatus.OPTIMAL) {
            System.out.println("x = " + solver.value(x));
            System.out.println("y = " + solver.value(y));
        }
    }
}