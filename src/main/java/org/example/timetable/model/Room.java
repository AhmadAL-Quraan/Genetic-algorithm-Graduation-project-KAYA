package org.example.timetable.model;

import org.example.timetable.model.enums.RoomType;
import java.util.Objects;

/**
 * Clean Room Class
 * مسؤول فقط عن تمثيل القاعة، بدون أي أكواد تخص قراءة الملفات (SRP)
 */
public class Room {
    private final String building;
    private final String number;
    private final RoomType roomType;

    public Room(String building, String number, RoomType roomType) {
        this.building = building;
        this.number = number;
        this.roomType = roomType;
    }

    // Getters لحماية البيانات من التعديل الخارجي
    public String getBuilding() { return building; }
    public String getNumber() { return number; }
    public RoomType getRoomType() { return roomType; }

    // الـ equals و الـ hashCode مهمين جداً عشان استخدام الـ HashMap والـ Set
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Room room = (Room) o;
        return Objects.equals(building, room.building) &&
                Objects.equals(number, room.number) &&
                roomType == room.roomType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(building, number, roomType);
    }

    @Override
    public String toString() {
        return building + " " + number + " (" + roomType + ")";
    }
}