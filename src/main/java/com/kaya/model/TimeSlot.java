package com.kaya.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a specific time block during the week (e.g., Sun/Tue 08:00 to 09:30).
 * Acts as a 'Gene' pool component in the Genetic Algorithm.
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TimeSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalTime startTime;
    private LocalTime endTime;

    // EAGER fetch is used here because the Genetic Algorithm needs the days immediately
    // to check for conflicts during the Fitness Calculation phase.
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<DayOfWeek> days;

    // Extracted as an Entity to allow dynamic additions (e.g., "In-Person", "Online").
    @ManyToOne
    @JoinColumn(name = "time_slot_type_id")
    private TimeSlotType timeSlotType;

    public TimeSlot(LocalTime startTime, LocalTime endTime, Set<DayOfWeek> days, TimeSlotType timeSlotType) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.days = days;
        this.timeSlotType = timeSlotType;
    }

    /**
     * Business Key Equality:
     * Compares TimeSlots based on actual time (Start, End, Days).
     * We exclude the 'timeSlotType' because the physical time block remains the same
     * regardless of the teaching method. This is critical for detecting overlaps in the GA.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeSlot timeSlot = (TimeSlot) o;
        return Objects.equals(startTime, timeSlot.startTime) &&
                Objects.equals(endTime, timeSlot.endTime) &&
                Objects.equals(days, timeSlot.days);
    }

    /**
     * Ensures high performance when caching TimeSlots in HashSets inside the PoolHelper.
     */
    @Override
    public int hashCode() {
        return Objects.hash(startTime, endTime, days);
    }

    @Override
    public String toString() {
        return "Days: " + days + " Start: " + startTime + " End: " + endTime;
    }
}