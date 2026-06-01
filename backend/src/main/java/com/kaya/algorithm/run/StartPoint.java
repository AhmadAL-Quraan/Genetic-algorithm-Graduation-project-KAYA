package com.kaya.algorithm.run;

import com.kaya.algorithm.EvolutionEngine;
import com.kaya.algorithm.GAConfig;
import com.kaya.algorithm.IslandManager;
import com.kaya.algorithm.ProgressSnapshot;
import com.kaya.model.Lecture;
import com.kaya.model.Room;
import com.kaya.model.TimeSlot;
import com.kaya.model.TimeTable;
import com.kaya.model.enums.RoomType;
import com.kaya.model.enums.TeachingMethod;

import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class StartPoint {

    public static void printData(List<Lecture> lecture, List<TimeSlot> allTimeSlots, List<Room> allRooms) {
        Map<String, List<Lecture>> deptYearGroups = new HashMap<>();

        for (Lecture c : lecture) {


            String deptYearKey =
                    c.getCourse().getCourseSymbol() + "-" +
                            c.getCourse().getCourseNumber().charAt(0);

            deptYearGroups.computeIfAbsent(deptYearKey, k -> new ArrayList<>()).add(c);
        }

        System.out.println("Time Slot Size "+ allTimeSlots.size());
        for (String key : deptYearGroups.keySet()){
            int courceOnline = 0;
            int courceLec = 0;
            int courceBli = 0;

            for (Lecture lecture1 : deptYearGroups.get(key)) {

                if (lecture1.getCourse().getTeachingMethod() == TeachingMethod.ONLINE){
                    courceOnline++;
                }
                else if (lecture1.getCourse().getTeachingMethod() == TeachingMethod.IN_PERSON ){
                        courceLec ++;
                }
                else {
                    courceBli ++;
                }
            }

            System.out.println(key +": "+ deptYearGroups.get(key).size());
            System.out.println("number of online: " + courceOnline);
            System.out.println("number of InPerson: " + courceLec);
            System.out.println("number of Blended: " + courceBli);

            System.out.println("----------------------");
        }

    }

    public static TimeTable runAlgorithm(List<Lecture> lectures,
                                         List<Room> allRooms,
                                         List<TimeSlot> allTimeSlots,
                                         GAConfig config,
                                         boolean useIslandModel,
                                         BooleanSupplier cancelCheck,
                                         Consumer<ProgressSnapshot> progressCallback) {

        System.out.println("Starting KAYA Timetable Scheduler from Database...");

        //printData(lectures, allTimeSlots, allRooms);
        Map<RoomType, HashSet<Room>> roomPools = new HashMap<>();
        Map<TeachingMethod, HashSet<TimeSlot>> timePools = new HashMap<>();

        roomPools.put(RoomType.LECTURE, new LinkedHashSet<>());
        roomPools.put(RoomType.LAB, new LinkedHashSet<>());
        roomPools.put(RoomType.OTHER, new LinkedHashSet<>());
        timePools.put(TeachingMethod.BLENDED, new LinkedHashSet<>());
        timePools.put(TeachingMethod.IN_PERSON, new LinkedHashSet<>());
        timePools.put(TeachingMethod.ONLINE, new LinkedHashSet<>());

        for (Room room : allRooms) {
            if (room.getRoomType() != null) {
                roomPools.get(room.getRoomType()).add(room);
            }
        }

        for (TimeSlot ts : allTimeSlots) {
            if (ts.getTeachingMethod() != null) {
                timePools.get(ts.getTeachingMethod()).add(ts);
            }
        }


        System.out.println("Initializing Population...");

        System.out.println("Starting Evolution Process...");
        ArrayList<TimeTable> finalPop ;
        ArrayList<TimeTable> initialPop;
        TimeTable bestSchedule;

        if (useIslandModel) {
            System.out.println("Feature Toggle: ENABLED -> Routing to the Island Manager...");

            // The IslandManager encapsulates the initialization, epoch loops, and parallel migrations.
            IslandManager islandManager = new IslandManager(config);

            // = engine.initializePopulation(
              //      new ArrayList<>(lectures), timePools, roomPools);

            // This single call triggers the Parallel Streams and returns the absolute global best schedule.
            bestSchedule = islandManager.runEvolution(new ArrayList<>(lectures), timePools, roomPools, cancelCheck, progressCallback);

        } else {
            System.out.println("Feature Toggle: DISABLED -> Routing to the Legacy Evolution Engine...");

            // Initialize the legacy engine with the injected Configuration
            EvolutionEngine engine = new EvolutionEngine(config);

            System.out.println("Initializing Generation 0 (Random Population)...");
            // Assuming the legacy initializePopulation signature matches this setup
            initialPop = engine.initializePopulation(new ArrayList<>(lectures), timePools, roomPools);

            System.out.println("Starting the Standard Evolution Process...");
            finalPop = engine.evolveGenerations(initialPop, timePools, roomPools, cancelCheck, progressCallback);

            // The evolveGenerations method returns the population sorted by fitness.
            // Therefore, the schedule at index 0 is the absolute best solution found.
            bestSchedule = finalPop.get(0);
        }
        //bestSchedule = finalPop.get(0);
        System.out.println("=====================================");
        System.out.println("Best Fitness Report:");
        System.out.println(bestSchedule.getReport().getTotalPenalty());
        System.out.println("=====================================");

        return bestSchedule;
    }
}