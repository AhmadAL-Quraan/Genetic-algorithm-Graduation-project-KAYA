package com.kaya.dto.response;

import com.kaya.model.Lecture;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FitnessReportResponse {
    private Long id;
    private Integer roomConflicts;
    private Integer instructorConflicts;
    private Integer studentConflicts;
    private Integer totalPenalty;
    private Set<Lecture> conflictingLectures;
}