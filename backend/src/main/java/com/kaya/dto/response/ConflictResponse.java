package com.kaya.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConflictResponse {
    private String type;
    private String message;
    private Long lectureAId;
    private Long lectureBId;
    private String courseA;
    private String courseB;
    private String instructorA;
    private String instructorB;
    private String timeSlot;
}