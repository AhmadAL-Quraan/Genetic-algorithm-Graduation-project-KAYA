package com.kaya.service;

import com.kaya.dto.request.RoomRequest;
import com.kaya.dto.response.RoomResponse;
import com.kaya.mapper.RoomMapper;
import com.kaya.model.Room;
import com.kaya.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;

    public List<RoomResponse> getAll() {
        return roomRepository.findAll()
                .stream()
                .map(RoomMapper::mapToResponse)
                .toList();
    }

    public RoomResponse getById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        return RoomMapper.mapToResponse(room);
    }

    public RoomResponse create(RoomRequest request) {
        Room response = new Room();
        return saveRoom(request, response);
    }

    public List<RoomResponse> createBulk(List<RoomRequest> request) {
        List<RoomResponse> l = new ArrayList<>();

        for (RoomRequest roomRequest : request) {
            Room response = new Room();
            l.add(saveRoom(roomRequest, response));
        }
        return l;
    }

    public RoomResponse update(Long id, RoomRequest request) {
        Room response = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        return saveRoom(request, response);
    }

    public void delete(Long id) {
        if (!roomRepository.existsById(id)) {
            throw new RuntimeException("Room not found");
        }
        roomRepository.deleteById(id);
    }

    public void deleteAll() {
        roomRepository.deleteAll();
    }

    // --- Helper methods --- //

    private RoomResponse saveRoom(RoomRequest request, Room response) {

        response.setRoomNumber(request.getRoomNumber());
        response.setBuilding(request.getBuilding());
        response.setRoomType(request.getRoomType());

        Room updated = roomRepository.save(response);
        return RoomMapper.mapToResponse(updated);
    }
}
