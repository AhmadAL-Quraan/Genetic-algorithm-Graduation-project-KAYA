package com.kaya.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeTableResponse {
    private Long id;
    private FitnessReportResponse fitnessReport;
    private List<LectureResponse> lectures;
}