package data;

import model.Course;
import model.Room;
import model.TimeSlot;

import java.util.*;

public class UniversityData {

    public List<Course> courses;
    public List<Room> rooms;
    public List<TimeSlot> slots;

    public UniversityData() {

        courses = new ArrayList<>();
        rooms = new ArrayList<>();
        slots = new ArrayList<>();

        // Rooms
        //rooms.add(new Room("R1", 30));
        //rooms.add(new Room("R2", 50));

        // TimeSlots
        //slots.add(new TimeSlot(0, "Mon-8"));
        //slots.add(new TimeSlot(1, "Mon-10"));
        //slots.add(new TimeSlot(2, "Tue-8"));

        // Courses
//        courses.add(new Course("Math", 0, 0, 1));
//        courses.add(new Course("Physics", 1, 0, 1));
//        courses.add(new Course("Programming", 2, 1, 1));
    }
}
