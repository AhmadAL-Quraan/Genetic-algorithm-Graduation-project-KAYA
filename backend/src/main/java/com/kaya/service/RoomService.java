package com.kaya.service;

import com.kaya.dto.request.RoomRequest;
import com.kaya.dto.response.RoomResponse;
import com.kaya.dto.mapper.RoomMapper;
import com.kaya.exception.CourseException;
import com.kaya.exception.RoomException;
import com.kaya.model.Room;
import com.kaya.repository.LectureRepository;
import com.kaya.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final LectureRepository lectureRepository;

    public List<RoomResponse> getAll() {
        return roomRepository.findAll()
                .stream()
                .map(RoomMapper::mapToResponse)
                .toList();
    }

    public RoomResponse getById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(RoomException::notFound);
        return RoomMapper.mapToResponse(room);
    }

    public Room getEntityById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(RoomException::notFound);
    }

    public RoomResponse create(RoomRequest request) {
        Room response = new Room();
        try {
            return saveRoom(request, response);
        } catch (DataIntegrityViolationException e) {
            throw RoomException.alreadyExists(
                    request.getBuilding().toUpperCase(),
                    request.getRoomNumber()
            );
        }
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
                .orElseThrow(RoomException::notFound);
        return saveRoom(request, response);
    }

    @Transactional
    public void delete(Long id) {
        if (!roomRepository.existsById(id)) {
            throw RoomException.notFound();
        }
        lectureRepository.detachRoom(id);
        roomRepository.deleteById(id);
    }

    @Transactional
    public void deleteAll() {
        lectureRepository.detachAllRooms();
        roomRepository.deleteAll();
    }

    private RoomResponse saveRoom(RoomRequest request, Room response) {
        response.setRoomNumber(request.getRoomNumber());
        response.setBuilding(request.getBuilding().toUpperCase());
        response.setRoomType(request.getRoomType());
        Room updated = roomRepository.save(response);
        return RoomMapper.mapToResponse(updated);
    }
}
