package org.example.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

class Class {
    public Course course;
    public int number;
    public String instructor;
    public TimeSlot time;
    public Room room;
    public int ID;

    Class(Course course, int number, String instructor, TimeSlot time, Room room, int ID) {
        this.course = course;
        this.number = number;
        this.instructor = instructor;
        this.time = time;
        this.room = room;
        this.ID = ID;
    }
    // A class can belong to more than one TimeSlot group; so in order to get the suitable TimeSlots for a specific
    // course I made this function. It returns the intersection of all time pools the course belong to.
    public HashSet<TimeSlot> getTimeSlots(Map<String, HashSet<TimeSlot>> timePools) {
        HashSet<TimeSlot> timeSlotsSet;
        timeSlotsSet = timePools.get(new ArrayList(this.course.timeGroups).getFirst());
        for (Map.Entry<String, HashSet<TimeSlot>> entry : timePools.entrySet()) {
            if (this.course.timeGroups.contains(entry.getKey())) {
                timeSlotsSet.retainAll(entry.getValue());
            }
        }
        return timeSlotsSet;
    }
    // A class can belong to more than one Room group; so in order to get the suitable Rooms for a specific
    // course I made this function. It returns the intersection of all room pools the course belong to.
    public HashSet<Room> getRooms(Map<String, HashSet<Room>> roomPools) {
        HashSet<Room> roomSlotsSet;
        roomSlotsSet = roomPools.get(new ArrayList(this.course.roomGroups).getFirst());
        for(Map.Entry<String, HashSet<Room>> entry : roomPools.entrySet())
        {
            if (this.course.roomGroups.contains(entry.getKey()))
                roomSlotsSet.retainAll(entry.getValue());
        }
        return roomSlotsSet;
}
    @Override
    public String toString() {
        //return "Course" + course + "Class no : " + number + ", Time : "  + time + ", room : " + room + "\n";
        return "ID : " + ID + ", Course : " + course + ", Class no : " + number + ", Instructor : " + instructor + ", Time : "  + time + ", Room : " + room;
    }
}