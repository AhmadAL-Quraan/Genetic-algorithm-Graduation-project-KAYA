package com.kaya.dto.response;

import com.kaya.model.RoomType;
import com.kaya.model.TimeSlotType;
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
    private RoomType roomGroups;
    private TimeSlotType timeSlotType;
}