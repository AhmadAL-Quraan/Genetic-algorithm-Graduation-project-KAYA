package com.kaya.algorithm;

import com.kaya.model.Lecture;
import com.kaya.model.Room;
import com.kaya.model.TimeSlot;
import com.kaya.model.enums.RoomType;
import com.kaya.model.enums.TeachingMethod;

import java.util.HashSet;
import java.util.Map;

/**
 * A highly optimized utility class for resource retrieval.
 * Uses HashMap lookups to achieve O(1) time complexity when finding valid
 * rooms and time slots for any given lecture based on its specific requirements.
 */
public class PoolHelper {

    /**
     * Retrieves all valid time slots that match the lecture's required teaching method.
     *
     * @param lecture   The lecture requiring a time slot.
     * @param timePools The master dictionary categorizing time slots by their type.
     * @return A defensive copy of the valid time slots HashSet.
     */
    public static HashSet<TimeSlot> getValidTimeSlots(Lecture lecture, Map<TeachingMethod, HashSet<TimeSlot>> timePools) {

        // Retrieve the specific teaching method required by this course
        TeachingMethod timeSlotType = lecture.getCourse().getTeachingMethod();

        // Defensive Copying: Return a NEW HashSet so the Genetic Algorithm
        // doesn't accidentally mutate the university's master time pool.
        return new HashSet<>(timePools.get(timeSlotType));
    }

    /**
     * Retrieves all valid rooms that match the lecture's required physical properties.
     *
     * @param lecture   The lecture requiring a room.
     * @param roomPools The master dictionary categorizing rooms by their type.
     * @return A defensive copy of the valid rooms HashSet.
     */
    public static HashSet<Room> getValidRooms(Lecture lecture, Map<RoomType, HashSet<Room>> roomPools) {

        // Retrieve the specific room type (e.g., Lab, Standard Lecture Hall) required by this course
        RoomType type = lecture.getCourse().getRequiredRoomType();

        // Defensive Copying to protect the master data structure
        return new HashSet<>(roomPools.get(type));
    }
}