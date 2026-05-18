package com.kaya.model;

import jakarta.persistence.*;
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
public class TimeTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "timetable_id")
    private List<Lecture> lectures;

    @OneToOne(cascade = CascadeType.MERGE, orphanRemoval = true)
    private FitnessReport report;

    public TimeTable(List<Lecture> lectures) {
        this.lectures = lectures;
        this.report = new FitnessReport();
    }

    @Override
    public String toString() {
        StringBuilder schedule = new StringBuilder();
        for (Lecture lecture : lectures) {
            schedule.append(lecture).append("\n");
        }
        return schedule.toString();
    }
}