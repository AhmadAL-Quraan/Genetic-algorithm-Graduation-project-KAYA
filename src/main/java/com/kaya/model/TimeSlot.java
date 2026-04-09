package com.kaya.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TimeSlot {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    public LocalTime startTime, endTime;
    @ElementCollection
    public Set<String> days;
    @ElementCollection
    public List<String> timeGroup;

    public TimeSlot(LocalTime startTime, LocalTime endTime, Set<String> days, List<String> timeGroup) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.days = days;
        this.timeGroup = timeGroup;
    }

    @Override
    public int hashCode() {
        return Objects.hash(startTime, endTime, days, timeGroup);
    }

    @Override
    public String toString() {
        return "Days : " + days + " Start : " + startTime + " End : " + endTime + " Group : " + timeGroup;
    }
}
