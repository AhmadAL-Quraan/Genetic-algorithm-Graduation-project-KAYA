package org.example.model;

import org.dhatim.fastexcel.reader.Row;

import java.util.ArrayList;
import java.util.Objects;

class Room {
    public String building;
    public String number;
    public ArrayList<String> groups;

    Room(String building, String number, ArrayList<String> groups) {
        this.building = building;
        this.number = number;
        this.groups = groups;
    }
    public static Room extractRoom(Row r)
    {
        String building;
        String number;
        //Room number extraction
        String input = r.getCellText(17); // مق 205
        // 1. trim() removes any sneaky spaces at the very beginning or end of the cell.
// 2. split("\\s+") splits the string wherever there is one OR MORE spaces.
        String[] parts = input.trim().split("\\s+");

// Always good to check the length just in case an Excel cell was formatted weirdly
        if (parts.length >= 2) {
            building = parts[0]; // "مق"
            number = parts[1];   // "205"
        } else {
            System.out.println("Could not split the string properly: " + input);
            return null;
        }

        return new Room(building, number, new ArrayList<String>());
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Room room = (Room) o;
        return Objects.equals(building, room.building) && Objects.equals(number, room.number)
                && Objects.equals(groups, room.groups);
    }

    @Override
    public int hashCode() {
        return Objects.hash(building, number, groups);
    }
    @Override
    public String toString() {
        return building + " " + number + " " + groups;
    }
}