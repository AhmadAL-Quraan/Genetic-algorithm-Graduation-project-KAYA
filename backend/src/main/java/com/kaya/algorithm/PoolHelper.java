package com.kaya.algorithm;

import com.kaya.model.Lecture;
import com.kaya.model.Room;
import com.kaya.model.TimeSlot;
import com.kaya.model.enums.RoomType;
import com.kaya.model.enums.TeachingMethod;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PoolHelper {

    /**
     * Returns the valid sub-slots for a lecture based on its course's teaching method.
     * The timePools already contain the generated sub-slots (built from time windows
     * in TimeTableService), so this is a simple teaching-method lookup.
     */
    public static HashSet<TimeSlot> getValidTimeSlots(Lecture lecture,
                                                       Map<TeachingMethod, HashSet<TimeSlot>> timePools) {
        TeachingMethod method = lecture.getCourse().getTeachingMethod();
        HashSet<TimeSlot> pool = timePools.get(method);
        return new HashSet<>(pool != null ? pool : Set.of());
    }

    public static HashSet<Room> getValidRooms(Lecture lecture, Map<RoomType, HashSet<Room>> roomPools) {
        RoomType type = lecture.getCourse().getRequiredRoomType();
        HashSet<Room> pool = roomPools.get(type);
        return new HashSet<>(pool != null ? pool : Set.of());
    }
}
