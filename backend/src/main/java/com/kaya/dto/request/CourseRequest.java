package com.kaya.dto.request;

import com.kaya.model.enums.RoomType;
import com.kaya.model.enums.TeachingMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Set;

@Data @NoArgsConstructor @AllArgsConstructor
public class CourseRequest {

    @NotBlank private String courseSymbol;
    @NotBlank private String courseNumber;
    @NotEmpty private List<String> majors;
    @NotNull  private RoomType roomGroups;
    @NotNull  private TeachingMethod timeGroups;

    private Long departmentId;

    private Long teacherId;
    private String instructor;
    private Long sectionNumber;

    private Long roomId;
    private String building;
    private String roomNumber;
    private RoomType roomType;

    private Long timeSlotId;
    private String startTime;
    private String endTime;
    private Set<DayOfWeek> days;
}
