package com.kaya.model;

// Removed the enum import since TeachingMethod is now an Entity in the same package
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Objects;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TimeSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalTime startTime; // 8
    private LocalTime endTime; // 9

    // [NO CHANGE HERE]: We keep DayOfWeek as an Enum because days of the week are static.
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<DayOfWeek> days;

    // [MODIFIED]: Replaced Enum with a ManyToOne relationship to the TeachingMethod entity.
    @ManyToOne
    @JoinColumn(name = "time_slot_type_id")
    private TimeSlotType timeSlotType;

    public TimeSlot(LocalTime startTime, LocalTime endTime, Set<DayOfWeek> days, TimeSlotType timeSlotType) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.days = days;
        this.timeSlotType = timeSlotType;
    }

    // Equals and HashCode to help the algorithm compare TimeSlots accurately
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeSlot timeSlot = (TimeSlot) o;
        return Objects.equals(startTime, timeSlot.startTime) &&
                Objects.equals(endTime, timeSlot.endTime) &&
                Objects.equals(days, timeSlot.days);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startTime, endTime, days);
    }

    @Override
    public String toString() {
        return "Days: " + days + " Start: " + startTime + " End: " + endTime;
    }
}