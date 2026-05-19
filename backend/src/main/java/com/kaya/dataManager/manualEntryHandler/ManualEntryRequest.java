package com.kaya.dataManager.manualEntryHandler;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ManualEntryRequest {

    // Course
    @NotNull private Long courseId;

    // Instructor
    @NotNull private Long instructorId;
}
