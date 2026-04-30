package com.kaya.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

/**
 * Defines the mode of teaching for a TimeSlot (e.g., "In-Person", "Online", "Hybrid").
 * Allows dynamic management of teaching modes without altering the source code.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class TimeSlotType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String typeName;

    public TimeSlotType(String typeName) {
        this.typeName = typeName;
    }

    /**
     * Crucial for the Genetic Algorithm Mapping structure.
     * Allows TimeSlotType to be correctly used as a Key in HashMaps
     * (e.g., Map<TimeSlotType, HashSet<TimeSlot>>).
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeSlotType that = (TimeSlotType) o;
        return Objects.equals(typeName, that.typeName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(typeName);
    }

    @Override
    public String toString() {
        return typeName;
    }
}