package com.kaya.model;

import jakarta.persistence.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Lecture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    @ManyToOne
    @JoinColumn(name = "time_slot_id")
    private TimeSlot timeSlot;

    @ManyToOne
    @JoinColumn(name = "instructor_id")
    private Instructor instructor;


    private Integer sectionNumber;

    @Column(name = "timetable_id", insertable = false, updatable = false)
    private Long timetableId;

    @Override
    public String toString() {

        return String.format(
                "ID: %d | Course: %s | Section: %d | Instructor: %s | Time: [%s] | Room: [%s]",
                id, course, sectionNumber, instructor, timeSlot, room
        );
    }
}
