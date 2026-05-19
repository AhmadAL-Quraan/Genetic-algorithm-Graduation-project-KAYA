package com.kaya.dto.response;

import com.kaya.model.enums.RoomType;
import com.kaya.model.enums.TeachingMethod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseResponse {
    private Long id;
    private String courseSymbol;
    private String courseNumber;
    private RoomType roomGroups;
    private TeachingMethod timeGroups;
}
