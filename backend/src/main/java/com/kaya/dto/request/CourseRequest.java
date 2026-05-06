package com.kaya.dto.request;

import com.kaya.model.enums.RoomType;
import com.kaya.model.enums.TeachingMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
public class CourseRequest {

    @NotBlank private String courseSymbol;
    @NotBlank private String courseNumber;
    @NotNull  private RoomType roomGroups;
    @NotNull  private TeachingMethod timeGroups;

    private Long teacherId;
    private String instructor;
    private Long sectionNumber;

    private List<String> majors;
    private Long departmentId;
    private Long roomId;
    private Long timeSlotId;
}
