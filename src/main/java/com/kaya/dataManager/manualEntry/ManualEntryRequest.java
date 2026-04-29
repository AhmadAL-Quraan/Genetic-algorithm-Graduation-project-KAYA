package com.kaya.dataManager.manualEntry;

import com.kaya.model.RoomType;
import com.kaya.model.TimeSlotType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ManualEntryRequest {

    // Course Data
    @NotBlank private String courseSymbol;
    @NotBlank private String courseNumber;
    @NotNull
    private RoomType requiredRoomType;
    @NotNull
    private TimeSlotType timeSlotType;

    // Instructor
    @NotBlank private String instructor;
}
