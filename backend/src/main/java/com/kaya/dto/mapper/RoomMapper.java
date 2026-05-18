package com.kaya.dto.mapper;

import com.kaya.dto.response.RoomResponse;
import com.kaya.model.Room;

public class RoomMapper {

    public static RoomResponse mapToResponse(Room room) {
        return new RoomResponse(
                room.getId(),
                room.getBuilding(),
                room.getRoomNumber(),
                room.getRoomType()
        );
    }

    public static Room mapToEntity(RoomResponse response) {
        return new Room(
                response.getId(),
                response.getBuilding(),
                response.getRoomNumber(),
                response.getRoomType()
        );
    }
}
