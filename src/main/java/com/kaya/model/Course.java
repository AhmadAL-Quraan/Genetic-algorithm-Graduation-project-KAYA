package com.kaya.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Course {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String courseSymbol;
    private String courseNumber;
    private List<String> majors;
    private Set<String> roomGroups;
    private Set<String> timeGroups;

    public Course(String courseSymbol, String courseNumber, List<String> majors, HashSet<String> roomGroups, HashSet<String> timeGroups) {
        this.courseSymbol = courseSymbol;
        this.courseNumber = courseNumber;
        this.majors = majors;
        this.roomGroups = roomGroups;
        this.timeGroups = timeGroups;
    }

    @Override
    public String toString() {
        return courseSymbol + " " + courseNumber;
    }
}
