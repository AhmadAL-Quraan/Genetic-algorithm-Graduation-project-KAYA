package com.kaya.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseResponse {
    private Long id;
    private String courseSymbol;
    private String courseNumber;
    private List<String> majors;
    private Set<String> roomGroups;
    private Set<String> timeGroups;
}