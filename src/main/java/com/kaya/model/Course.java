package com.kaya.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Represents the blueprint/metadata of a university course (e.g., CS 101).
 * This entity does NOT hold spatial or temporal data (Room/TimeSlot).
 * Instead, it defines the *requirements* needed for any lecture of this course.
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String courseSymbol; // e.g., "CS"
    private String courseNumber; // e.g., "101"

    /**
     * Defines the physical requirement for this course (e.g., "Computer Lab").
     * The Genetic Algorithm's PoolHelper uses this to fetch valid rooms in O(1) time.
     */
    @ManyToOne
    @JoinColumn(name = "required_room_type_id")
    private RoomType requiredRoomType;

    /**
     * Defines the temporal/teaching method requirement (e.g., "Online", "In-Person").
     * Guides the GA to only assign TimeSlots that match this teaching method.
     */
    @ManyToOne
    @JoinColumn(name = "time_slot_type_id")
    private TimeSlotType timeSlotType;

    public Course(String courseSymbol, String courseNumber, List<String> majors, RoomType requiredRoomType, TimeSlotType timeSlotType) {
        this.courseSymbol = courseSymbol;
        this.courseNumber = courseNumber;
        this.requiredRoomType = requiredRoomType;
        this.timeSlotType = timeSlotType;
    }

    @Override
    public String toString() {
        return courseSymbol + " " + courseNumber;
    }
}