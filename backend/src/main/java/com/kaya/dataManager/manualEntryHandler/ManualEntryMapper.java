package com.kaya.dataManager.manualEntryHandler;

public class ManualEntryMapper {

    public static ManualEntryResponse mapToResponse(ManualEntry manualEntry) {
        return new ManualEntryResponse(
                manualEntry.getId(),
                manualEntry.getCourseId(),
                manualEntry.getInstructorId()
        );
    }
}
