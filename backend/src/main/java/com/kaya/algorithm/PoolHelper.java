package com.kaya.algorithm;

import com.kaya.model.Lecture;
import com.kaya.model.Room;
import com.kaya.model.TimeSlot;
import com.kaya.model.enums.RoomType;
import com.kaya.model.enums.TeachingMethod;

import java.util.HashSet;
import java.util.Map;

public class PoolHelper {

    public static HashSet<TimeSlot> getValidTimeSlots(Lecture lecture, Map<TeachingMethod, HashSet<TimeSlot>> timePools) {
        if (lecture.getCourse() == null || lecture.getCourse().getTeachingMethod() == null) return new HashSet<>();
        TeachingMethod method = lecture.getCourse().getTeachingMethod();
        HashSet<TimeSlot> pool = timePools.get(method);
        return pool != null ? new HashSet<>(pool) : new HashSet<>();
    }

    public static HashSet<Room> getValidRooms(Lecture lecture, Map<RoomType, HashSet<Room>> roomPools) {
        if (lecture.getCourse() == null || lecture.getCourse().getRequiredRoomType() == null) return new HashSet<>();
        RoomType type = lecture.getCourse().getRequiredRoomType();
        HashSet<Room> pool = roomPools.get(type);
        return pool != null ? new HashSet<>(pool) : new HashSet<>();
    }
}
