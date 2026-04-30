package com.kaya.mapper;

import com.kaya.dto.response.FitnessReportDTO;
import com.kaya.dto.response.TimeTableResponse;
import com.kaya.model.FitnessReport;
import com.kaya.model.TimeTable;

public class TimeTableMapper {

    public static TimeTableResponse mapToResponse(TimeTable t) {
        FitnessReportDTO reportDto = null;
        FitnessReport r = t.getReport();
        if (r != null) {
            reportDto = new FitnessReportDTO();
            reportDto.setRoomConflicts(r.getRoomConflicts());
            reportDto.setInstructorConflicts(r.getInstructorConflicts());
            reportDto.setStudentConflicts(r.getStudentConflicts());
            reportDto.setTotalPenalty(r.getTotalPenalty());
        }
        return new TimeTableResponse(
                t.getId(),
                t.getFitness(),
                t.getGeneratedAt(),
                reportDto,
                t.getLectures() == null ? java.util.List.of() :
                        t.getLectures().stream().map(LectureMapper::mapToResponse).toList()
        );
    }
}
