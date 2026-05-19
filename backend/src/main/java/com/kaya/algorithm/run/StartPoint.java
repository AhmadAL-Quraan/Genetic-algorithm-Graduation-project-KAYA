package com.kaya.algorithm.run;

import com.kaya.algorithm.EvolutionEngine;
import com.kaya.algorithm.GAConfig;
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

    public static TimeTable runAlgorithm(List<Lecture> lectures,
                                         List<Room> allRooms,
                                         List<TimeSlot> allTimeSlots,
                                         GAConfig config,
                                         boolean useIslandModel,
                                         BooleanSupplier cancelCheck,
                                         Consumer<ProgressSnapshot> progressCallback) {

        System.out.println("Starting KAYA Timetable Scheduler from Database...");

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

        EvolutionEngine engine = new EvolutionEngine(config);

        System.out.println("Initializing Population...");
        ArrayList<TimeTable> initialPop = engine.initializePopulation(
                new ArrayList<>(lectures), timePools, roomPools);

        System.out.println("Starting Evolution Process...");
        ArrayList<TimeTable> finalPop = engine.evolveGenerations(
                initialPop, timePools, roomPools, cancelCheck, progressCallback);

        TimeTable bestSchedule = finalPop.get(0);
        System.out.println("=====================================");
        System.out.println("Best Fitness Report:");
        System.out.println(bestSchedule.getReport());
        System.out.println("=====================================");

        return bestSchedule;
    }
}
