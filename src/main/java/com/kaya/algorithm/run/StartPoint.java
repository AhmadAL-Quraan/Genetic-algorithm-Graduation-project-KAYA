package com.kaya.algorithm.run;

import com.kaya.algorithm.EvolutionEngine;
import com.kaya.algorithm.GAConfig;
import com.kaya.model.Lecture;
import com.kaya.model.Room;
import com.kaya.model.TimeSlot;
import com.kaya.model.TimeTable;
import com.kaya.model.enums.RoomType;
import com.kaya.model.enums.TeachingMethod;

import java.util.*;

public class StartPoint {

    public static TimeTable runFromDatabase(List<Lecture> lectures, List<Room> allRooms, List<TimeSlot> allTimeSlots) {

        System.out.println("Starting KAYA Timetable Scheduler from Database...");

        // 1. Data Preparation
        Map<RoomType, HashSet<Room>> roomPools = new HashMap<>();
        Map<TeachingMethod, HashSet<TimeSlot>> timePools = new HashMap<>();

        roomPools.put(RoomType.LECTURE, new LinkedHashSet<>());
        roomPools.put(RoomType.LAB, new LinkedHashSet<>());
        timePools.put(TeachingMethod.BLENDED, new LinkedHashSet<>());
        timePools.put(TeachingMethod.IN_PERSON, new LinkedHashSet<>());
        timePools.put(TeachingMethod.ONLINE, new LinkedHashSet<>());

        // Filling the RoomPools from Database
        for (Room room : allRooms) {
            if (room.getRoomType() != null) {
                roomPools.get(room.getRoomType()).add(room);
            }
        }

        // Filling the TimeSlots from Database
        for (TimeSlot ts : allTimeSlots) {
            if (ts.getTeachingMethod() != null) {
                timePools.get(ts.getTeachingMethod()).add(ts);
            }
        }

        // 2. Algorithm Configuration
        GAConfig config = new GAConfig();

        // 3. Initialize Engine
        EvolutionEngine engine = new EvolutionEngine(config);

        // 4. Execution
        System.out.println("Initializing Population...");
        // حولنا الـ List لـ ArrayList عشان الـ Engine بيطلبها كده
        ArrayList<TimeTable> initialPop = engine.initializePopulation(new ArrayList<>(lectures), timePools, roomPools);

        System.out.println("Starting Evolution Process...");
        ArrayList<TimeTable> finalPop = engine.evolveGenerations(initialPop, timePools, roomPools);

        // 5. Results
        TimeTable bestSchedule = finalPop.get(0);
        System.out.println("=====================================");
        System.out.println("Best Fitness Report:");
        System.out.println(bestSchedule.getReport());
        System.out.println("=====================================");

        System.out.println(bestSchedule);

        return bestSchedule;
    }
}