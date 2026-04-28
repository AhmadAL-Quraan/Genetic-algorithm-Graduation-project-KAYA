package com.kaya.dto.request;

import com.kaya.model.enums.TeachingMethod;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlotRequest {
    @NotNull private LocalTime startTime;
    @NotNull private LocalTime endTime;
    @NotEmpty private Set<DayOfWeek> days;
    @NotNull private TeachingMethod teachingMethod;
}