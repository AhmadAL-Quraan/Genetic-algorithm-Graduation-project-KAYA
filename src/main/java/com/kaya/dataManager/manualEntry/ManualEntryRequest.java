package com.kaya.dataManager.manualEntry;

import com.kaya.model.enums.RoomType;
import com.kaya.model.enums.TeachingMethod;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
    @NotNull @Enumerated(EnumType.STRING)
    private RoomType requiredRoomType;
    @NotNull @Enumerated(EnumType.STRING)
    private TeachingMethod teachingMethod;

    // Instructor
    @NotBlank private String instructor;
}
