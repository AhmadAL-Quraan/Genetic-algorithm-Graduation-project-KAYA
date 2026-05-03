package com.kaya.model;

import com.kaya.model.enums.RoomType;
import com.kaya.model.enums.TeachingMethod;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String courseSymbol;
    private String courseNumber;

    // [تعديل]: بدل Set، خلينا الكورس يطلب نوع قاعة واحد (معمل أو قاعة عادية)
    @Enumerated(EnumType.STRING)
    private RoomType requiredRoomType;

    // [تعديل]: بدل Set، خلينا الكورس يتبع طريقة تدريس واحدة (مدمج، وجاهي، الخ)
    @Enumerated(EnumType.STRING)
    private TeachingMethod teachingMethod;

    public Course(String courseSymbol, String courseNumber, RoomType requiredRoomType, TeachingMethod teachingMethod) {
        this.courseSymbol = courseSymbol;
        this.courseNumber = courseNumber;
        this.requiredRoomType = requiredRoomType;
        this.teachingMethod = teachingMethod;
    }

    @Override
    public String toString() {
        return courseSymbol + " " + courseNumber;
    }
}