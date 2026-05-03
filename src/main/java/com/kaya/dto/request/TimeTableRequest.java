package com.kaya.dto.request;

import com.kaya.model.Lecture;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeTableRequest {
    private FitnessReportRequest fitnessReport;
    @NotEmpty private List<Lecture> lectures;
}