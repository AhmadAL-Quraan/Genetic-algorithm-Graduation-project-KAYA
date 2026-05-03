package com.kaya.dto.request;

import com.kaya.model.Lecture;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FitnessReportRequest {
    @NotNull private Long roomConflicts;
    @NotNull private Long instructorConflicts;
    @NotNull private Long studentConflicts;
    @NotNull private Long totalPenalty;
    private Set<Lecture> conflictingLectures;
}
