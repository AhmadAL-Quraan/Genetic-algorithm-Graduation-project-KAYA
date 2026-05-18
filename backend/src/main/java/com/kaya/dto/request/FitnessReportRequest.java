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
    @NotNull private Integer roomConflicts;
    @NotNull private Integer instructorConflicts;
    @NotNull private Integer studentConflicts;
    @NotNull private Integer totalPenalty;
    private Set<Lecture> conflictingLectures;
}
