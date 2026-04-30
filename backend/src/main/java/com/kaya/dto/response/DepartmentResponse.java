package com.kaya.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class DepartmentResponse {
    private Long id;
    private String name;
    private String code;
}
