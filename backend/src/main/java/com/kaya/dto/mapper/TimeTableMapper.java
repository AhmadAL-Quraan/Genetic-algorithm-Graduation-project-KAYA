package com.kaya.dto.mapper;

import com.kaya.dto.request.TimeTableRequest;
import com.kaya.dto.response.TimeTableResponse;
import com.kaya.model.TimeTable;

public class TimeTableMapper {

    public static TimeTableResponse mapToResponse(TimeTable timeTable) {
        Integer fitness = null;
        if (timeTable.getReport() != null) {
            fitness = timeTable.getReport().getTotalPenalty();
        }
        String generatedAt = timeTable.getGeneratedAt() != null
                ? timeTable.getGeneratedAt().toString() : null;

        return new TimeTableResponse(
                timeTable.getId(),
                fitness,
                generatedAt,
                FitnessReportMapper.mapToResponse(timeTable.getReport()),
                timeTable.getLectures()
                        .stream()
                        .map(LectureMapper::mapToResponse)
                        .toList()
        );
    }

    public static TimeTable mapToEntity(TimeTableResponse response) {
        return new TimeTable(
                response.getId(),
                response.getLectures().stream()
                        .map(LectureMapper::mapToEntity)
                        .toList(),
                FitnessReportMapper.mapToEntity(response.getFitnessReport()),
                null
        );
    }

    public static TimeTableRequest mapToRequest(TimeTable request) {
        return new TimeTableRequest(
                FitnessReportMapper.mapToRequest(request.getReport()),
                request.getLectures()
        );
    }
}
