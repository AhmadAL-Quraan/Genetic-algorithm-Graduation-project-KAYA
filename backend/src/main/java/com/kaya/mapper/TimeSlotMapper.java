package com.kaya.mapper;

import com.kaya.dto.response.TimeSlotResponse;
import com.kaya.model.TimeSlot;

public class TimeSlotMapper {

    public static TimeSlotResponse mapToResponse(TimeSlot t) {
        return new TimeSlotResponse(
                t.getId(),
                t.getStartTime(),
                t.getEndTime(),
                t.getDays(),
                t.getTeachingMethod()
        );
    }
}
