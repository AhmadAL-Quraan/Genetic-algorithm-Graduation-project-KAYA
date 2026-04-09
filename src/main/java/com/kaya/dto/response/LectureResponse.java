package com.kaya.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LectureResponse {
    private Long id;
    private CourseResponse course;
<<<<<<< HEAD
    private Long number;
=======
    private Integer number;
>>>>>>> 2f1def42acd0dd54877d40fc787cdfb45bbf7ddf
    private String instructor;
    private TimeSlotResponse timeSlot;
    private RoomResponse room;
}