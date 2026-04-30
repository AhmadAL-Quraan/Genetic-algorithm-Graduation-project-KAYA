package com.kaya.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class TeacherRequest {
    @NotBlank private String name;
    private String email;
    private Long departmentId;
}
