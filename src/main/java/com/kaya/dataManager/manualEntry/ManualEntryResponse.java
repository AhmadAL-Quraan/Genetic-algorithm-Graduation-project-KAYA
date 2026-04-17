package com.kaya.dataManager.manualEntry;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ManualEntryResponse {

    @NotBlank private Long courseId;
    @NotBlank private String instructor;
}
