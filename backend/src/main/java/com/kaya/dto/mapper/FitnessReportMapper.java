package com.kaya.dto.mapper;

import com.kaya.dto.request.FitnessReportRequest;
import com.kaya.dto.response.FitnessReportResponse;
import com.kaya.model.FitnessReport;

import java.util.HashSet;

public class FitnessReportMapper {

    public static FitnessReportResponse mapToResponse(FitnessReport fitnessReport) {
        if (fitnessReport == null) return null;
        return new FitnessReportResponse(
                fitnessReport.getId(),
                fitnessReport.getRoomConflicts(),
                fitnessReport.getInstructorConflicts(),
                fitnessReport.getStudentConflicts(),
                fitnessReport.getTotalPenalty()
        );
    }

    public static FitnessReport mapToEntity(FitnessReportResponse response) {
        if (response == null) return null;
        FitnessReport report = new FitnessReport();
        report.setId(response.getId());
        report.setRoomConflicts(response.getRoomConflicts());
        report.setInstructorConflicts(response.getInstructorConflicts());
        report.setStudentConflicts(response.getStudentConflicts());
        report.setTotalPenalty(response.getTotalPenalty());
        report.setConflictingLectures(new HashSet<>());
        return report;
    }

    public static FitnessReportRequest mapToRequest(FitnessReport fitnessReport) {
        if (fitnessReport == null) return null;
        return new FitnessReportRequest(
                fitnessReport.getRoomConflicts(),
                fitnessReport.getInstructorConflicts(),
                fitnessReport.getStudentConflicts(),
                fitnessReport.getTotalPenalty(),
                new HashSet<>()
        );
    }
}
