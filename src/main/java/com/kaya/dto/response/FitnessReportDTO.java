package com.kaya.dto.response;

import lombok.Data;

@Data
public class FitnessReportDTO {
    private Long roomConflicts;
    private Long instructorConflicts;
    private Long studentConflicts;
    private Long totalPenalty;
}