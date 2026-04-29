package com.kaya.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

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

    // [MODIFIED]: Replaced Enum with a ManyToOne relationship to the RoomType entity.
    // This allows users to dynamically add new room types from the frontend.
    @ManyToOne
    @JoinColumn(name = "room_type_id")
    private RoomType roomType;

    public Room(String building, String roomNumber, RoomType roomType) {
        this.building = building;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
    }

    // Senior Tip: Comparison is done by building and room number only, as they uniquely identify a room.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Room room = (Room) o;
        return Objects.equals(building, room.building) && Objects.equals(roomNumber, room.roomNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(building, roomNumber);
    }

    @Override
    public String toString() {
        return building + " " + roomNumber + " " + (roomType != null ? roomType.getTypeName() : "null");
    }
}