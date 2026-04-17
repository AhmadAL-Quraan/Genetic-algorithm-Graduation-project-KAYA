package com.kaya.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LectureRequest {
    @NotNull private Long courseId;
    private Long number;
    @NotBlank private String instructor;
    private Long timeSlotId;
    private Long roomId;
}