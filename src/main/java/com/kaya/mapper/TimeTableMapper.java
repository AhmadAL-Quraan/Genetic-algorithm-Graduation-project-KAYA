package com.kaya.mapper;

import com.kaya.dto.response.TimeTableResponse;
import com.kaya.model.TimeTable;

import java.time.LocalDateTime;

public class TimeTableMapper {

    public static TimeTableResponse mapToResponse(TimeTable timeTable) {
        return new TimeTableResponse(
                timeTable.getId(),
                timeTable.getFitness(),
                LocalDateTime.now(),
                null,
                timeTable.getLectures()
                        .stream()
                        .map(LectureMapper::mapToResponse)
                        .toList()
        );
    }
}
