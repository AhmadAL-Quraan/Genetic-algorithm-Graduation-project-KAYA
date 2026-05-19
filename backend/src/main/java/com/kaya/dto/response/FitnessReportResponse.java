package com.kaya.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FitnessReportResponse {
    private Long id;
    private Integer roomConflicts;
    private Integer instructorConflicts;
    private Integer studentConflicts;
    private Integer totalPenalty;
}
