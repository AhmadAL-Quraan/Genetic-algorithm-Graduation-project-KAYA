package com.kaya.dataManager.manualEntry;

public class ManualEntryMapper {

    public static ManualEntryResponse mapToDTO(ManualEntry entry) {
        Long instructorId = entry.getInstructor() != null ? entry.getInstructor().getId() : null;
        String instructorName = entry.getInstructor() != null ? entry.getInstructor().getInstructorName() : null;
        return new ManualEntryResponse(
                entry.getId(),
                entry.getCourseId(),
                instructorId,
                instructorName
        );
    }
}
