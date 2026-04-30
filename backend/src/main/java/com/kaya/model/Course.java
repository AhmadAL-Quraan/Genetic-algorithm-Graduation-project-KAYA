package com.kaya.model;

import com.kaya.model.enums.RoomType;
import com.kaya.model.enums.TeachingMethod;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String courseSymbol;
    private String courseNumber;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> majors;

    @Enumerated(EnumType.STRING)
    private RoomType requiredRoomType;

    @Enumerated(EnumType.STRING)
    private TeachingMethod teachingMethod;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id")
    private Department department;

    public Course(String courseSymbol, String courseNumber, List<String> majors,
                  RoomType requiredRoomType, TeachingMethod teachingMethod) {
        this.courseSymbol = courseSymbol;
        this.courseNumber = courseNumber;
        this.majors = majors;
        this.requiredRoomType = requiredRoomType;
        this.teachingMethod = teachingMethod;
    }

    @Override
    public String toString() { return courseSymbol + " " + courseNumber; }
}
