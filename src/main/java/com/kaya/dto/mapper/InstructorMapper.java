package com.kaya.dto.mapper;

import com.kaya.dto.response.InstructorResponse;
import com.kaya.model.Instructor;

public class InstructorMapper {

    public static InstructorResponse mapToResponse(Instructor instructor) {
        return new InstructorResponse(
                instructor.getId(),
                instructor.getInstructorName()
        );
    }

    public static Instructor mapToEntity(InstructorResponse response) {
        return new Instructor(
                response.getId(),
                response.getInstructorName()
        );
    }
}
