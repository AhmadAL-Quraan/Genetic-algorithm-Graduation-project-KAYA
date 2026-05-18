package com.kaya.dto.mapper;

import com.kaya.dto.request.TimeTableRequest;
import com.kaya.dto.response.TimeTableResponse;
import com.kaya.model.TimeTable;

public class TimeTableMapper {

    public static TimeTableResponse mapToResponse(TimeTable timeTable) {
        return new TimeTableResponse(
                timeTable.getId(),
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
                FitnessReportMapper.mapToEntity(response.getFitnessReport())
        );
    }

    public static TimeTableRequest mapToRequest(TimeTable request) {
        return new TimeTableRequest(
                FitnessReportMapper.mapToRequest(request.getReport()),
                request.getLectures()
        );
    }
}
