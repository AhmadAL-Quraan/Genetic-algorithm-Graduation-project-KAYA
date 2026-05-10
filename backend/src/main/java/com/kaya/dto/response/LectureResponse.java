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
    private TeacherInfo teacher;
    private String instructor;
    private TimeSlotResponse timeSlot;
    private RoomResponse room;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeacherInfo {
        private Long id;
        private String name;
    }
}
