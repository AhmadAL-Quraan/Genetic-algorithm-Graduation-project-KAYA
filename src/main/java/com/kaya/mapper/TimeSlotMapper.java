package com.kaya.mapper;

import com.kaya.dto.response.TimeSlotResponse;
import com.kaya.model.TimeSlot;

public class TimeSlotMapper {

    public static TimeSlotResponse mapToResponse(TimeSlot timeSlot) {
        return new TimeSlotResponse(
                timeSlot.getId(),
                timeSlot.getStartTime(),
                timeSlot.getEndTime(),
                timeSlot.getDays()
        );
    }
}
