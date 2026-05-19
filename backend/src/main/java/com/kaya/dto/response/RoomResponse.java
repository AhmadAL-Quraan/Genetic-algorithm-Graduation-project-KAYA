package com.kaya.dto.response;

import com.kaya.model.enums.RoomType;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomResponse {
    private Long id;
    private String building;
    private String roomNumber;
    private RoomType roomType;
}