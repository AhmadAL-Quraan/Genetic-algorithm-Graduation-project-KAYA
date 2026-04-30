package com.kaya.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class DepartmentRequest {
    @NotBlank private String name;
    @NotBlank private String code;
}
