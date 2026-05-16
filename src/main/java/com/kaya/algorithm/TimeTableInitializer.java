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

/**
 * Utility class dedicated to the construction of the initial population (Generation 0).
 * Ensures that the first generation of schedules is populated with valid, randomly
 * assigned resources before the evolutionary process begins.
 */
public class TimeTableInitializer {

    /**
     * Iterates through every lecture in an empty TimeTable and assigns a random,
     * requirement-compliant Room and TimeSlot based on the dynamically generated pools.
     *
     * @param tt        The newly created TimeTable (Chromosome) to be initialized.
     * @param timePools The master map containing categorized available time slots.
     * @param roomPools The master map containing categorized available rooms.
     */
    public static void initializeRandomly(TimeTable tt,
                                          Map<TeachingMethod, HashSet<TimeSlot>> timePools,
                                          Map<RoomType, HashSet<Room>> roomPools) {

        List<TimeSlot> timePool;
        List<Room> roomPool;
        Random rand = new Random();

        for (Lecture c : tt.getLectures()) {
            // Fetch dynamically constrained pools using the highly optimized O(1) PoolHelper
            timePool = new ArrayList<>(getValidTimeSlots(c, timePools));
            roomPool = new ArrayList<>(getValidRooms(c, roomPools));

            // Safety Check: Ensure the pool is not empty before attempting a random assignment
            // to prevent IndexOutOfBoundsException in highly constrained datasets.
            if (!timePool.isEmpty()) {
                c.setTimeSlot(timePool.get(rand.nextInt(timePool.size())));
            }
            if (!roomPool.isEmpty()) {
                c.setRoom(roomPool.get(rand.nextInt(roomPool.size())));
            }
        }

        // Establish the baseline fitness score for this randomly generated schedule
        FitnessCalculator.calculateFitness(tt);
    }
}