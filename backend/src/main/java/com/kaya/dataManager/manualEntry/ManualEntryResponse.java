package com.kaya.dataManager.manualEntry;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ManualEntryResponse {
    private Long courseId;
    private String instructor;
}
