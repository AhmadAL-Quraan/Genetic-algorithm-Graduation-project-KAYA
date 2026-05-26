package com.kaya.dataManager.manualEntryHandler;

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

    private Long courseId;
    private Long instructorId;
}
