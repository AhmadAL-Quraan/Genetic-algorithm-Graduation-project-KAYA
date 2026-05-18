package com.kaya.dataManager.manualEntryHandler;

public class ManualEntryMapper {

    public static ManualEntryResponse mapToDTO(ManualEntry dataManager) {
        return new ManualEntryResponse(
                dataManager.getCourseId(),
                dataManager.getInstructorId()
        );
    }
}