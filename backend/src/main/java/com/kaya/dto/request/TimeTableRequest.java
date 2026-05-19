package com.kaya.dto.request;

import com.kaya.model.Lecture;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeTableRequest {
    private FitnessReportRequest fitnessReport;
    private List<Lecture> lectures;
}
