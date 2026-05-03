package com.kaya.dto.mapper;

import com.kaya.dto.response.TimeSlotResponse;
import com.kaya.model.TimeSlot;

public class TimeSlotMapper {

    public static TimeSlotResponse mapToResponse(TimeSlot timeSlot) {
        return new TimeSlotResponse(
                timeSlot.getId(),
                timeSlot.getStartTime(),
                timeSlot.getEndTime(),
                timeSlot.getDays(),
                timeSlot.getTeachingMethod()
        );
    }

    public static TimeSlot mapToEntity(TimeSlotResponse response) {
        return new TimeSlot(
                response.getId(),
                response.getStartTime(),
                response.getEndTime(),
                response.getDays(),
                response.getTeachingMethod()
        );
    }
}
