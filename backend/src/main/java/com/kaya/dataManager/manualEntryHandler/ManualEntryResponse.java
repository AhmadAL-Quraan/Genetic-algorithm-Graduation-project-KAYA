package com.kaya.dataManager.manualEntryHandler;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ManualEntryResponse {
    private Long id;
    private Long courseId;
    private Long instructorId;
}
