package com.kaya.dataManager.manualEntry;

public class ManualEntryMapper {

    public static ManualEntryResponse mapToDTO(ManualEntry dataManager) {
        return new ManualEntryResponse(
                dataManager.getCourseId(),
                dataManager.getInstructor()
        );
    }
}