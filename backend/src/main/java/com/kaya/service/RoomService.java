package com.kaya.service;

import com.kaya.dto.request.RoomRequest;
import com.kaya.dto.response.RoomResponse;
import com.kaya.mapper.RoomMapper;
import com.kaya.model.Lecture;
import com.kaya.model.Room;
import com.kaya.repository.LectureRepository;
import com.kaya.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final LectureRepository lectureRepository;

    public List<RoomResponse> getAll() {
        return roomRepository.findAll().stream().map(RoomMapper::mapToResponse).toList();
    }

    public RoomResponse getById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        return RoomMapper.mapToResponse(room);
    }

    public RoomResponse create(RoomRequest request) {
        return saveRoom(request, new Room());
    }

    public RoomResponse update(Long id, RoomRequest request) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        return saveRoom(request, room);
    }

    @Transactional
    public void delete(Long id) {
        if (!roomRepository.existsById(id)) throw new RuntimeException("Room not found");
        List<Lecture> lectures = lectureRepository.findAll().stream()
                .filter(l -> l.getRoom() != null && id.equals(l.getRoom().getId()))
                .toList();
        lectures.forEach(l -> l.setRoom(null));
        lectureRepository.saveAll(lectures);
        roomRepository.deleteById(id);
    }

    @Transactional
    public void deleteAll() {
        lectureRepository.findAll().forEach(l -> l.setRoom(null));
        lectureRepository.saveAll(lectureRepository.findAll());
        roomRepository.deleteAll();
    }

    private RoomResponse saveRoom(RoomRequest request, Room room) {
        room.setRoomNumber(request.getRoomNumber());
        room.setBuilding(request.getBuilding());
        room.setRoomType(request.getRoomType());
        return RoomMapper.mapToResponse(roomRepository.save(room));
    }
}
