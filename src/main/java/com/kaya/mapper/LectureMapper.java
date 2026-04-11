package com.kaya.mapper;

import com.kaya.dto.response.LectureResponse;
import com.kaya.model.Lecture;

public class LectureMapper {

    public static LectureResponse mapToResponse(Lecture lecture) {
        return new LectureResponse(
                lecture.getId(),
                CourseMapper.mapToResponse(lecture.getCourse()),
                lecture.getSectionNumber(),
                lecture.getInstructor(),
                TimeSlotMapper.mapToResponse(lecture.getTimeSlot()),
                RoomMapper.mapToResponse(lecture.getRoom())
        );
    }
}
