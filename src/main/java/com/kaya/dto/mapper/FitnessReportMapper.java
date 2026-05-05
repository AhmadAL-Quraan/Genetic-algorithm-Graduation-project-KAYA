package com.kaya.dto.mapper;

import com.kaya.dto.request.FitnessReportRequest;
import com.kaya.dto.response.FitnessReportResponse;
import com.kaya.model.FitnessReport;

public class FitnessReportMapper {

    public static FitnessReportResponse mapToResponse(FitnessReport fitnessReport) {
        return new FitnessReportResponse(
                fitnessReport.getId(),
                fitnessReport.getRoomConflicts(),
                fitnessReport.getInstructorConflicts(),
                fitnessReport.getStudentConflicts(),
                fitnessReport.getTotalPenalty(),
                fitnessReport.getConflictingLectures()
        );
    }

    public static FitnessReport mapToEntity(FitnessReportResponse response) {
        return new FitnessReport(
                response.getId(),
                response.getRoomConflicts(),
                response.getInstructorConflicts(),
                response.getStudentConflicts(),
                response.getTotalPenalty(),
                response.getConflictingLectures()
        );
    }

    public static FitnessReportRequest mapToRequest(FitnessReport fitnessReport) {
        return new FitnessReportRequest(
                fitnessReport.getRoomConflicts(),
                fitnessReport.getInstructorConflicts(),
                fitnessReport.getStudentConflicts(),
                fitnessReport.getTotalPenalty(),
                fitnessReport.getConflictingLectures()
        );
    }
}
