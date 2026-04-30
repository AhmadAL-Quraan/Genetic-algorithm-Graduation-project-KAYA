package com.kaya.mapper;

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
}
