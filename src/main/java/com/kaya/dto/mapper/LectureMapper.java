package com.kaya.dto.mapper;

import com.kaya.dto.response.LectureResponse;
import com.kaya.model.Lecture;

public class LectureMapper {

    public static LectureResponse mapToResponse(Lecture lecture) {
        return new LectureResponse(
                lecture.getId(),
                CourseMapper.mapToResponse(lecture.getCourse()),
                lecture.getSectionNumber(),
                lecture.getInstructor(),
                lecture.getTimeSlot() != null
                        ? TimeSlotMapper.mapToResponse(lecture.getTimeSlot())
                        : null,
                lecture.getRoom() != null
                        ? RoomMapper.mapToResponse(lecture.getRoom())
                        : null
        );
    }

    public static Lecture mapToEntity(LectureResponse response) {
        return new Lecture(
                response.getId(),
                CourseMapper.mapToEntity(response.getCourse()),
                null,
                null,
                null,
                response.getInstructor()
        );
    }

}
