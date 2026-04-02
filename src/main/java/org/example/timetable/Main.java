package org.example.timetable;

import org.example.timetable.algorithm.GeneticAlgorithm;
import org.example.timetable.algorithm.Schedule;
import org.example.timetable.data.ExcelDataLoader;
import org.example.timetable.data.TimeSlotFactory;
import org.example.timetable.model.CourseOffering;
import org.example.timetable.model.Room;
import org.example.timetable.model.TimeSlot;
import org.example.timetable.model.enums.RoomType;

import java.util.*;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        System.out.println("Starting University Timetable Generator...");

        // 1. Load Data
        ExcelDataLoader loader = new ExcelDataLoader();
        loader.loadData("2526_first_term_sched.xlsx");

        List<CourseOffering> baseOfferings = loader.getOfferings();
        Set<Room> allRooms = loader.getRooms();

        if (baseOfferings.isEmpty()) {
            System.err.println("No offerings loaded. Check the file name and format.");
            return;
        }

        System.out.println("Successfully loaded " + baseOfferings.size() + " course offerings.");

        // 2. Prepare Pools for GA
        Map<RoomType, List<Room>> roomPools = new HashMap<>();
        for (Room r : allRooms) {
            roomPools.computeIfAbsent(r.getRoomType(), k -> new ArrayList<>()).add(r);
        }

        // Generate TimeSlots programmatically using the Factory
        List<TimeSlot> allTimeSlots = TimeSlotFactory.generateTimeSlots();

        // Split TimeSlots into On-Site (Morning) and Online (Evening)
        List<TimeSlot> onSiteTimePool = allTimeSlots.stream()
                .filter(t -> !t.isOnline())
                .collect(Collectors.toList());

        List<TimeSlot> onlineTimePool = allTimeSlots.stream()
                .filter(TimeSlot::isOnline)
                .collect(Collectors.toList());

        // 3. Configure GA Parameters
        int populationSize = 100;
        int maxGenerations = 1000;
        int tournamentSize = 7;
        double mutationRate = 0.05;
        int elitismCount = 2;

        GeneticAlgorithm ga = new GeneticAlgorithm(
                populationSize, maxGenerations, tournamentSize, mutationRate, elitismCount
        );

        // 4. Run Evolution
        System.out.println("Starting Evolution...");
        Schedule bestSchedule = ga.evolve(baseOfferings, roomPools, onSiteTimePool, onlineTimePool);

        // 5. Print Results
        System.out.println("\n--- Evolution Finished ---");
        System.out.println("Final Best Fitness: " + bestSchedule.getFitness());
        System.out.println("\nSample of the generated schedule:");

        bestSchedule.getOfferings().stream()
                .limit(200)
                .forEach(System.out::println);
    }
}