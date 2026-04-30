package com.kaya.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class Lecture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "room_id")
    private Room room;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "time_slot_id")
    private TimeSlot timeSlot;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "teacher_id")
    private Teacher teacher;

    private Long sectionNumber;
    private String instructor;

    @Override
    public String toString() {
        return String.format("ID: %d | Course: %s | Section: %d | Instructor: %s | Time: [%s] | Room: [%s]",
                id, course, sectionNumber, instructor != null ? instructor : (teacher != null ? teacher.getName() : "—"),
                timeSlot, room);
    }
}
