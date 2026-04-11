package com.kaya.algorithm;

import com.kaya.model.Lecture;
import com.kaya.model.Room;
import com.kaya.model.TimeSlot;
import com.kaya.model.TimeTable;

import java.util.*;

public class TimeTableInitializer {

    // نقلنا الميثود هنا وبقت بتاخد الـ TimeTable كـ Parameter عشان تعدل عليه
    public static void initializeRandomly(TimeTable tt, Map<String, HashSet<TimeSlot>> timePools, Map<String, HashSet<Room>> roomPools) {
        List<TimeSlot> timePool;
        List<Room> roomPool;

        Random rand = new Random();
        for (Lecture c : tt.lectures) {
            timePool = new ArrayList<>(com.kaya.algorithm.PoolHelper.getValidTimeSlots(c, timePools));
            roomPool = new ArrayList<>(com.kaya.algorithm.PoolHelper.getValidRooms(c, roomPools));

            // Randomly pick an allele (or a gene) from the pools
            c.setTimeSlot(timePool.get(rand.nextInt(timePool.size())) );
            c.setRoom(roomPool.get(rand.nextInt(roomPool.size())) );
        }

        // حساب التقييم بعد التهيئة
        com.kaya.algorithm.FitnessCalculator.calculateFitness(tt);
    }
}