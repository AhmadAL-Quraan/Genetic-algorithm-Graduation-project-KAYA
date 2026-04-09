package com.kaya.mapper;

import com.kaya.dto.response.CourseResponse;
import com.kaya.model.Course;

public class CourseMapper {

    public static CourseResponse mapToResponse(Course course) {
        return new CourseResponse(
                course.getId(),
                course.getCourseSymbol(),
                course.getCourseNumber(),
                course.getMajors(),
                course.getRoomGroups(),
                course.getTimeGroups()
        );
    }
}
