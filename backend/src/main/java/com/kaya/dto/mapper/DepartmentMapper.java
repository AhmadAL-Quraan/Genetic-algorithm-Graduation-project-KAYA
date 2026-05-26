package com.kaya.dto.mapper;

import com.kaya.dto.response.DepartmentResponse;
import com.kaya.model.Department;

public class DepartmentMapper {

    public static DepartmentResponse mapToResponse(Department department) {
        return new DepartmentResponse(
                department.getId(),
                department.getName(),
                department.getCode()
        );
    }
}
