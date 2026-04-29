package com.kaya.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String courseSymbol;
    private String courseNumber;

    // [MODIFIED]: Replaced Enum with a ManyToOne relationship to the Lookup Table (RoomType)
    @ManyToOne
    @JoinColumn(name = "required_room_type_id")
    private RoomType requiredRoomType;

    // [MODIFIED]: Replaced Enum with a ManyToOne relationship to the Lookup Table (TeachingMethod)
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