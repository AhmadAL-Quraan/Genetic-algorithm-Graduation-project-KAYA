package org.example.timetable.model;

import java.util.Objects;

/**
 * Clean CourseOffering Class (كان يُسمى Class سابقاً)
 * يمثل الشعبة المطروحة فعلياً والتي تحتاج إلى جدولة.
 * يجمع بين الكورس، المحاضر، القاعة، والوقت.
 */
public class CourseOffering {
    private final int id; // رقم مميز لكل شعبة (لأن الكورس ممكن يكون ليه أكتر من شعبة)
    private final Course course;
    private final Instructor instructor;

    // هدول مش Final عشان خوارزمية الجينات تقدر تغيرهم في الـ Mutation
    private TimeSlot timeSlot;
    private Room room;

    public CourseOffering(int id, Course course, Instructor instructor) {
        this.id = id;
        this.course = course;
        this.instructor = instructor;
    }

    // Constructor إضافي في حالة أردنا عمل نسخة (Copy) مفيد جداً في الـ Crossover
    public CourseOffering(int id, Course course, Instructor instructor, TimeSlot timeSlot, Room room) {
        this.id = id;
        this.course = course;
        this.instructor = instructor;
        this.timeSlot = timeSlot;
        this.room = room;
    }

    // Getters
    public int getId() { return id; }
    public Course getCourse() { return course; }
    public Instructor getInstructor() { return instructor; }
    public TimeSlot getTimeSlot() { return timeSlot; }
    public Room getRoom() { return room; }

    // Setters (لعمليات التعديل في الـ Genetic Algorithm)
    public void setTimeSlot(TimeSlot timeSlot) { this.timeSlot = timeSlot; }
    public void setRoom(Room room) { this.room = room; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CourseOffering that = (CourseOffering) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        String timeStr = (timeSlot != null) ? timeSlot.toString() : "Unassigned";
        String roomStr = (room != null) ? room.toString() : "Unassigned";
        return String.format("Offering ID: %d | %s | Inst: %s | Time: %s | Room: %s",
                id, course, instructor.getName(), timeStr, roomStr);
    }
}