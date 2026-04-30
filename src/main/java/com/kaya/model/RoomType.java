package com.kaya.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

/**
 * Defines the classification of a Room (e.g., Lab, Lecture Hall).
 * Extracted as a standalone Entity to allow dynamic additions via the UI.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class RoomType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String typeName;

    public RoomType(String typeName) {
        this.typeName = typeName;
    }

    /**
     * Crucial for the Genetic Algorithm's Mapping structure.
     * Allows RoomType to be correctly used as a Key in HashMaps (e.g., Map<RoomType, HashSet<Room>>).
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoomType roomType = (RoomType) o;
        return Objects.equals(typeName, roomType.typeName);
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