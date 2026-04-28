package com.kaya.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeTableResponse {
    private Long id;
    private Long fitness;
    private LocalDateTime generatedAt;
    private FitnessReportDTO fitnessReport;
    private List<LectureResponse> lectures;
}