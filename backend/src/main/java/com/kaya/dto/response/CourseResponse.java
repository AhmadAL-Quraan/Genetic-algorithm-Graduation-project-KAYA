package com.kaya.dto.response;

import com.kaya.model.enums.RoomType;
import com.kaya.model.enums.TeachingMethod;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data @NoArgsConstructor
public class CourseResponse {
    private Long id;
    private String courseSymbol;
    private String courseNumber;
    private List<String> majors;
    private RoomType roomGroups;
    private TeachingMethod timeGroups;

    private DepartmentResponse department;

    private Long lectureId;
    private TeacherResponse teacher;
    private String instructor;
    private Long sectionNumber;
    private RoomResponse room;
    private TimeSlotResponse timeSlot;
}
