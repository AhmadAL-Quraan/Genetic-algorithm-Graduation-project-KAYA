package com.kaya.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TimeTable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    public Long fitness;
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "timetable_id")
    public List<Lecture> lectures;
    @Transient
    public FitnessReport report;

    public TimeTable(List<Lecture> lectures) {
        this.fitness = 0L;
        this.lectures = lectures;
        this.report = new FitnessReport();
    }

    @Override
    public String toString() {
        StringBuilder schedule;
        schedule = new StringBuilder();
        for (Lecture lecture : lectures) {
            schedule.append(lecture).append("\n");
        }
        return schedule.toString();
    }
}
