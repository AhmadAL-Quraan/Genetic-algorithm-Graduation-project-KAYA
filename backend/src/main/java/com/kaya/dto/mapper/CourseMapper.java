package com.kaya.dto.mapper;

import com.kaya.dto.response.CourseResponse;
import com.kaya.model.Course;

public class CourseMapper {

    public static CourseResponse mapToResponse(Course course) {
        return new CourseResponse(
                course.getId(),
                course.getCourseSymbol(),
                course.getCourseNumber(),
                course.getRequiredRoomType(),
                course.getTeachingMethod()
        );
    }

    public static Course mapToEntity(CourseResponse response) {
        Course c = new Course();
        c.setId(response.getId());
        c.setCourseSymbol(response.getCourseSymbol());
        c.setCourseNumber(response.getCourseNumber());
        c.setRequiredRoomType(response.getRoomGroups());
        c.setTeachingMethod(response.getTimeGroups());
        return c;
    }
}
