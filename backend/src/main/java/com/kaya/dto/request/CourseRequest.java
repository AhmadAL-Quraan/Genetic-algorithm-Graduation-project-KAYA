package com.kaya.dto.request;

import com.kaya.model.enums.RoomType;
import com.kaya.model.enums.TeachingMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseRequest {
    @NotBlank private String courseSymbol;
    @NotBlank private String courseNumber;
    @NotNull private RoomType roomGroups;
    @NotNull private TeachingMethod timeGroups;
}