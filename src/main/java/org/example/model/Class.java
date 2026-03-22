package org.example.model;

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

    @Override
    public String toString() {
        return "ID : " + ID + ", Course : " + course + ", Class no : " + number + ", Instructor : " + instructor + ", Time : "  + time + ", Room : " + room;
    }
}