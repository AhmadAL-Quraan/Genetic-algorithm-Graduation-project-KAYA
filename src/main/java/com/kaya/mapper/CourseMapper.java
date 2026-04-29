package com.kaya.mapper;

import com.kaya.dto.response.CourseResponse;
import com.kaya.dto.response.LectureResponse;
import com.kaya.model.Course;
import com.kaya.model.Lecture;

public class CourseMapper {

    public static CourseResponse mapToResponse(Course course) {
        return new CourseResponse(
                course.getId(),
                course.getCourseSymbol(),
                course.getCourseNumber(),
                course.getRequiredRoomType(),
                course.getTimeSlotType()
        );
    }

    public static Course mapToEntity(CourseResponse response) {
        return new Course(
                response.getId(),
                response.getCourseSymbol(),
                response.getCourseNumber(),
                response.getRoomGroups(),
                response.getTimeSlotType()
        );
    }
}
