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
    private Long roomConflicts;
    private Long instructorConflicts;
    private Long studentConflicts;
    private Long totalPenalty;
    private Set<Lecture> conflictingLectures;
}