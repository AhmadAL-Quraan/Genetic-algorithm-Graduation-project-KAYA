package com.kaya.dto.mapper;

import com.kaya.dto.response.InstructorResponse;
import com.kaya.model.Instructor;

public class InstructorMapper {

    public static InstructorResponse mapToResponse(Instructor instructor) {
        return new InstructorResponse(
                instructor.getId(),
                instructor.getInstructorName(),
                instructor.getEmail(),
                instructor.getDepartment() != null
                        ? DepartmentMapper.mapToResponse(instructor.getDepartment())
                        : null
        );
    }

    public static Instructor mapToEntity(InstructorResponse response) {
        Instructor instructor = new Instructor();
        instructor.setId(response.getId());
        instructor.setInstructorName(response.getName());
        return instructor;
    }
}
