package com.kaya.algorithm.run;

import com.kaya.algorithm.EvolutionEngine;
import com.kaya.algorithm.GAConfig;
import com.kaya.model.Lecture;
import com.kaya.model.Room;
import com.kaya.model.TimeSlot;
import com.kaya.model.TimeTable;

// [تعديل]: غيرنا الـ Imports لأن الأنواع مبقتش Enums
import com.kaya.model.RoomType;
import com.kaya.model.TimeSlotType;

import java.util.*;

public class StartPoint {

    public static void runFromDatabase(List<Lecture> lectures, List<Room> allRooms, List<TimeSlot> allTimeSlots) {

        System.out.println("Starting KAYA Timetable Scheduler from Database...");

        // 1. Data Preparation
        Map<RoomType, HashSet<Room>> roomPools = new HashMap<>();
        Map<TimeSlotType, HashSet<TimeSlot>> timePools = new HashMap<>();

        // [تعديل جوهري]: التعبئة الديناميكية (Dynamic Population)
        // بنلف على كل القاعات، ولو لقينا نوع جديد بنكريتله Set، وبعدين نضيف القاعة
        for (Room room : allRooms) {
            RoomType type = room.getRoomType();
            if (type != null) {
                // putIfAbsent: دالة ذكية بتعمل Set جديد بس لو النوع ده مش موجود في الخريطة
                roomPools.putIfAbsent(type, new LinkedHashSet<>());
                roomPools.get(type).add(room);
            }
        }

        // نفس الفكرة للأوقات وطرق التدريس
        for (TimeSlot ts : allTimeSlots) {
            TimeSlotType method = ts.getTimeSlotType();
            if (method != null) {
                timePools.putIfAbsent(method, new LinkedHashSet<>());
                timePools.get(method).add(ts);
            }
        }

        // 2. Algorithm Configuration
        GAConfig config = new GAConfig();

        // 3. Initialize Engine
        EvolutionEngine engine = new EvolutionEngine(config);

        // 4. Execution
        System.out.println("Initializing Population...");
        ArrayList<TimeTable> initialPop = engine.initializePopulation(new ArrayList<>(lectures), timePools, roomPools);

        System.out.println("Starting Evolution Process...");
        ArrayList<TimeTable> finalPop = engine.evolveGenerations(initialPop, timePools, roomPools);

        // 5. Results
        TimeTable bestSchedule = finalPop.get(0);
        System.out.println("=====================================");
        System.out.println("Best Fitness Report:");
        System.out.println(bestSchedule.getReport());
        System.out.println("=====================================");

        // System.out.println(bestSchedule); // شيل الكومنت لو عايز تطبع الجدول نفسه
    }
}