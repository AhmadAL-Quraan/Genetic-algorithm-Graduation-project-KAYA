package com.kaya.algorithm;

import com.kaya.model.Lecture;
import com.kaya.model.Room;
import com.kaya.model.TimeSlot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

public class PoolHelper {

    // A class can belong to more than one TimeSlot group; so in order to get the suitable TimeSlots for a specific
    // course I made this function. It returns the intersection of all time pools the course belongs to.
    public static HashSet<TimeSlot> getValidTimeSlots(Lecture lecture, Map<String, HashSet<TimeSlot>> timePools) {
        // تم استبدال getFirst() بـ get(0)
        HashSet<TimeSlot> timeSlotsSet = new HashSet<>(timePools.get(new ArrayList<>(lecture.getCourse().getTimeGroups()).get(0)));

        for (Map.Entry<String, HashSet<TimeSlot>> entry : timePools.entrySet()) {
            if (lecture.getCourse().getTimeGroups().contains(entry.getKey())) {
                timeSlotsSet.retainAll(entry.getValue());
            }
        }
        return timeSlotsSet;
    }

    // A class can belong to more than one Room group; so in order to get the suitable Rooms for a specific
    // course I made this function. It returns the intersection of all room pools the course belongs to.
    public static HashSet<Room> getValidRooms(Lecture lecture, Map<String, HashSet<Room>> roomPools) {
        // تم استبدال getFirst() بـ get(0)
        HashSet<Room> roomSlotsSet = new HashSet<>(roomPools.get(new ArrayList<>(lecture.getCourse().getRoomGroups()).get(0)));

        for(Map.Entry<String, HashSet<Room>> entry : roomPools.entrySet()) {
            if (lecture.getCourse().getRoomGroups().contains(entry.getKey())) {
                roomSlotsSet.retainAll(entry.getValue());
            }
        }
        return roomSlotsSet;
    }
}