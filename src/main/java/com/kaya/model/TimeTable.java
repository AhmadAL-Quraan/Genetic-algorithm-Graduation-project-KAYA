package com.kaya.model;

import com.kaya.dto.response.FitnessReportDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

// شلنا @Component لأن ده Data Model مش Spring Bean
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TimeTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // خليناهم private احتراماً للـ Encapsulation
    private Long fitness;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "timetable_id")
    private List<Lecture> lectures;

    @Transient
    private FitnessReport report;

    public TimeTable(List<Lecture> lectures) {
        this.fitness = 0L;
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