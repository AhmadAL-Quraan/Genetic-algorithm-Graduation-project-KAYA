package com.kaya.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Lecture {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne @JoinColumn(name = "course_id")
    private Course course;
    @ManyToOne @JoinColumn(name = "room_id")
    private Room room;
    @ManyToOne @JoinColumn(name = "time_slot_id")
    private TimeSlot timeSlot;

    private Long number;
    private String instructor;

    @Override
    public String toString() {
        //return "Course" + course + "Class no : " + number + ", Time : "  + time + ", room : " + room + "\n";
        return "ID : " + id + ", Course : " + course + ", Class no : " + number + ", Instructor : " + instructor + ", Time : "  + timeSlot + ", Room : " + room;
    }

}
