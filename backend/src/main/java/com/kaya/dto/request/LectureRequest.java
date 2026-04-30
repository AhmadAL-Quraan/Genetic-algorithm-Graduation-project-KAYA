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
    @NotNull  private Long courseId;
    @NotBlank private String instructor;
    private Long number;       // section number; defaults to 1 if null
    private Long timeSlotId;   // optional — assigned by GA when null
    private Long roomId;       // optional — assigned by GA when null
}
