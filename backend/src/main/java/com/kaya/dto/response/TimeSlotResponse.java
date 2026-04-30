package com.kaya.dto.response;

import com.kaya.model.enums.TeachingMethod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlotResponse {
    private Long id;
    private LocalTime startTime;
    private LocalTime endTime;
    private Set<DayOfWeek> days;
    private TeachingMethod teachingMethod;
}
