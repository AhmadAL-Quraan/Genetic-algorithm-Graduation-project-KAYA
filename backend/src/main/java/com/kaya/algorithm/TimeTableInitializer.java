package com.kaya.algorithm;

import com.kaya.model.Lecture;
import com.kaya.model.Room;
import com.kaya.model.TimeSlot;
import com.kaya.model.TimeTable;
import com.kaya.model.enums.RoomType;
import com.kaya.model.enums.TeachingMethod;

import java.util.*;

import static com.kaya.algorithm.PoolHelper.getValidRooms;
import static com.kaya.algorithm.PoolHelper.getValidTimeSlots;

public class TimeTableInitializer {

    public static void initializeRandomly(TimeTable tt,
                                          Map<TeachingMethod, HashSet<TimeSlot>> timePools,
                                          Map<RoomType, HashSet<Room>> roomPools) {
        List<TimeSlot> timePool;
        List<Room> roomPool;

        Random rand = new Random();
        for (Lecture c : tt.getLectures()) {
            timePool = new ArrayList<>(getValidTimeSlots(c, timePools));
            roomPool = new ArrayList<>(getValidRooms(c, roomPools));

            if (!timePool.isEmpty()) {
                c.setTimeSlot(timePool.get(rand.nextInt(timePool.size())));
            }
            if (!roomPool.isEmpty()) {
                c.setRoom(roomPool.get(rand.nextInt(roomPool.size())));
            }
        }

        FitnessCalculator.calculateFitness(tt);
    }
}
