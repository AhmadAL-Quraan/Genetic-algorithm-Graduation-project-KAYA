package com.kaya.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
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
<<<<<<< HEAD
    private String roomNumber;
    @ElementCollection
    private List<String> groups;

    public Room(String building, String roomNumber, List<String> groups) {
        this.building = building;
        this.roomNumber = roomNumber;
=======
    private String number;
    @ElementCollection
    private List<String> groups;

    public Room(String building, String number, List<String> groups) {
        this.building = building;
        this.number = number;
>>>>>>> 2f1def42acd0dd54877d40fc787cdfb45bbf7ddf
        this.groups = groups;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Room room = (Room) o;
<<<<<<< HEAD
        return Objects.equals(building, room.building) && Objects.equals(roomNumber, room.roomNumber)
=======
        return Objects.equals(building, room.building) && Objects.equals(number, room.number)
>>>>>>> 2f1def42acd0dd54877d40fc787cdfb45bbf7ddf
                && Objects.equals(groups, room.groups);
    }

    @Override
    public int hashCode() {
<<<<<<< HEAD
        return Objects.hash(building, roomNumber, groups);
    }
    @Override
    public String toString() {
        return building + " " + roomNumber + " " + groups;
=======
        return Objects.hash(building, number, groups);
    }
    @Override
    public String toString() {
        return building + " " + number + " " + groups;
>>>>>>> 2f1def42acd0dd54877d40fc787cdfb45bbf7ddf
    }
}
