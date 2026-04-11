package com.kaya.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Room {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String building;
    private String roomNumber;
    @ElementCollection
    private List<String> groups;

    public Room(String building, String roomNumber, List<String> groups) {
        this.building = building;
        this.roomNumber = roomNumber;
        this.groups = groups;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Room room = (Room) o;
        return Objects.equals(building, room.building) && Objects.equals(roomNumber, room.roomNumber)
                && Objects.equals(groups, room.groups);
    }

    @Override
    public int hashCode() {
        return Objects.hash(building, roomNumber, groups);
    }
    @Override
    public String toString() {
        return building + " " + roomNumber + " " + groups;
    }
}
