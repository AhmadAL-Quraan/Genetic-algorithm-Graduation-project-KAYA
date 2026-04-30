package com.kaya.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

/**
 * Represents a physical Room in the university.
 * Uses Business Key Equality (building + roomNumber) to ensure consistent
 * identification across Collections and JPA contexts without relying on database IDs.
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String building;
    private String roomNumber;

    @ManyToOne
    @JoinColumn(name = "room_type_id")
    private RoomType roomType;

    public Room(String building, String roomNumber, RoomType roomType) {
        this.building = building;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
    }

    /**
     * Compares rooms based on their unique business identifiers.
     * We strictly exclude the 'roomType' from this comparison because the physical identity
     * of a room (Building + Number) remains the same even if its type/purpose changes.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Room room = (Room) o;
        return Objects.equals(building, room.building) && Objects.equals(roomNumber, room.roomNumber);
    }

    /**
     * Generates a hash code strictly based on the business keys to maintain
     * high performance in Hash-based collections (e.g., HashSet in the Genetic Algorithm).
     */
    @Override
    public int hashCode() {
        return Objects.hash(building, roomNumber);
    }

    @Override
    public String toString() {
        return building + " " + roomNumber + " " + (roomType != null ? roomType.getTypeName() : "null");
    }
}