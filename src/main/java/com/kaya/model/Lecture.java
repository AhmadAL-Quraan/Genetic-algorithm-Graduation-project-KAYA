package com.kaya.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The core "Gene" of the Genetic Algorithm.
 * Represents a specific section/instance of a Course, assigned to a specific Instructor,
 * Room, and TimeSlot. The GA actively mutates the 'room' and 'timeSlot' fields to resolve conflicts.
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Lecture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The blueprint/requirements for this lecture. (Immutable by the GA)
    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    // The spatial dimension: Where this lecture takes place. (Mutable by GA)
    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    // The temporal dimension: When this lecture takes place. (Mutable by GA)
    @ManyToOne
    @JoinColumn(name = "time_slot_id")
    private TimeSlot timeSlot;

    private Long sectionNumber;
    private String instructor;

    @Override
    public String toString() {
        return String.format("ID: %d | Course: %s | Section: %d | Instructor: %s | Time: [%s] | Room: [%s]",
                id, course, sectionNumber, instructor, timeSlot, room);
    }
}