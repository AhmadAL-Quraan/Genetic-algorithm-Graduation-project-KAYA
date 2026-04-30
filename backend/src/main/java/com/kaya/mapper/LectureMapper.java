package com.kaya.mapper;

import com.kaya.dto.response.LectureResponse;
import com.kaya.model.Lecture;

public class LectureMapper {

    public static LectureResponse mapToResponse(Lecture lecture) {
        return new LectureResponse(
                lecture.getId(),
                lecture.getCourse() == null ? null : CourseMapper.mapToResponse(lecture.getCourse()),
                lecture.getSectionNumber(),
                lecture.getInstructor(),
                lecture.getTimeSlot() == null ? null : TimeSlotMapper.mapToResponse(lecture.getTimeSlot()),
                lecture.getRoom() == null ? null : RoomMapper.mapToResponse(lecture.getRoom())
        );
    }
}
