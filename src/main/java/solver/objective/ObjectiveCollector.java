package solver.objective;

import com.google.ortools.sat.*;

import java.util.List;

public class ObjectiveCollector {

    private CpModel model;

    private List<IntVar> allPenalties;

    public ObjectiveCollector(
            CpModel model,
            List<IntVar> allPenalties) {

        this.model = model;
        this.allPenalties = allPenalties;
    }

    public void buildObjective() {

        if (!allPenalties.isEmpty()) {

            model.minimize(
                    LinearExpr.sum(
                            allPenalties.toArray(
                                    new IntVar[0]
                            )
                    )
            );
        }

        System.out.println(
                "Combined Objective Created ✔"
        );
    }
}