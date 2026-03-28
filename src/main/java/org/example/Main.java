package org.example;

import com.google.ortools.Loader;
import service.TimetableService;

public class Main {

    public static void main(String[] args) {

        Loader.loadNativeLibraries();

        TimetableService service =
                new TimetableService();

        service.run(
                "data/schedule.csv"
        );
    }
}

/*
Teacher Compact Penalties = 1416
Balanced Section Penalties = 936
Lab Penalties = 630
Solver Status = OPTIMAL


200 < x < 300
  < 200
 111
 101

Solver Status = OPTIMAL
Objective Value = 18.0
Student Level Penalties = 120


 */