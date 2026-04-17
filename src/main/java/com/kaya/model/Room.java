package com.kaya.model;

import com.kaya.model.enums.RoomType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;
import java.util.Set;

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

    // استخدمنا Set بدل List لمنع التكرار
    // FetchType.EAGER عشان الخوارزمية بتسحب الداتا دي على طول فنوفر وقت الداتا بيز
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING) // السر هنا: عشان تتخزن كنص في الداتا بيز
    private RoomType roomType;

    public Room(String building, String roomNumber, RoomType roomType) {
        this.building = building;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
    }

    // Senior Tip: المقارنة بتتم بالمبنى ورقم القاعة بس، لأنهم بيميزوا أي قاعة في الجامعة
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
        return building + " " + roomNumber + " " + roomType;
    }
}