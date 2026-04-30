package com.kaya.algorithm.run;

import com.kaya.algorithm.EvolutionEngine;
import com.kaya.algorithm.GAConfig;
import com.kaya.model.Lecture;
import com.kaya.model.Room;
import com.kaya.model.TimeSlot;
import com.kaya.model.TimeTable;
import com.kaya.algorithm.data.ExcelParser;
import com.kaya.model.enums.RoomType;
import com.kaya.model.enums.TeachingMethod;

import java.util.*;

// هذا الكلاس مسؤول عن إدارة دورة حياة البرنامج بالكامل
public class SchedulerApp {

    public static void run() {
        System.out.println("Starting KAYA Timetable Scheduler...");

        // 1. Data Preparation
        //Map<String, HashSet<Room>> roomPools = new HashMap<>();
        //Map<String, HashSet<TimeSlot>> timePools = new HashMap<>();
        Map<RoomType, HashSet<Room>> roomPools = new HashMap<>();
        Map<TeachingMethod, HashSet<TimeSlot>> timePools = new HashMap<>();


        System.out.println("Parsing Excel Data...");
        ArrayList<Lecture> lectures = ExcelParser.excelParsing(roomPools, timePools);

        // 2. Algorithm Configuration
        GAConfig config = new GAConfig();
        // تقدر تغير أي إعداد هنا بسهولة لو حبيت، مثلاً:
        // config.maxGenerations = 500;

        // 3. Initialize Engine
        EvolutionEngine engine = new EvolutionEngine(config);

        // 4. Execution
        System.out.println("Initializing Population...");
        ArrayList<TimeTable> initialPop = engine.initializePopulation(lectures, timePools, roomPools);

        System.out.println("Starting Evolution Process...");
        ArrayList<TimeTable> finalPop = engine.evolveGenerations(initialPop, timePools, roomPools);

        // 5. Results
        TimeTable bestSchedule = finalPop.get(0);
        System.out.println("=====================================");
        System.out.println("Best Fitness Report:");
        System.out.println(bestSchedule.getReport());
        System.out.println("=====================================");
        System.out.println(bestSchedule); // شيل الكومنت لو عايز تطبع الجدول نفسه
    }
}