package com.kaya.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomRequest {
    @NotBlank private String building;
<<<<<<< HEAD
    @NotBlank private String roomNumber;
=======
    @NotBlank private String number;
>>>>>>> 2f1def42acd0dd54877d40fc787cdfb45bbf7ddf
}