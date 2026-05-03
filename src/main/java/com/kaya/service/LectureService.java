package com.kaya.service;

import com.kaya.dto.request.LectureRequest;
import com.kaya.dto.response.LectureResponse;
import com.kaya.dto.mapper.LectureMapper;
import com.kaya.model.Lecture;
import com.kaya.repository.LectureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LectureService {

    // Direct access
    private final LectureRepository lectureRepository;

    // Access by service
    private final CourseService courseService;
    private final RoomService roomService;
    private final TimeSlotService timeSlotService;

    public List<LectureResponse> getAll() {
        return lectureRepository.findAll()
                .stream()
                .map(LectureMapper::mapToResponse)
                .toList();
    }

    public LectureResponse getById(Long id) {
        Lecture lecture = lectureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lecture not found"));

        return LectureMapper.mapToResponse(lecture);
    }

    public LectureResponse create(LectureRequest request) {
        Lecture response = new Lecture();
        return saveLecture(request, response);
    }

    public LectureResponse update(Long id, LectureRequest request) {
        Lecture response = lectureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lecture not found"));

        return saveLecture(request, response);
    }

    public void delete(Long id) {
        if (!lectureRepository.existsById(id)) {
            throw new RuntimeException("Lecture not found");
        }
        lectureRepository.deleteById(id);
    }

    public void deleteAll() {
        lectureRepository.deleteAll();
    }

    // --- Helper methods --- //

    private LectureResponse saveLecture(LectureRequest request, Lecture response) {

        if (request.getCourseId() != null) {
            response.setCourse(courseService.getEntityById(request.getCourseId()));
        }
        if (request.getRoomId() != null) {
            response.setRoom(roomService.getEntityById(request.getRoomId()));
        }
        if (request.getTimeSlotId() != null) {
            response.setTimeSlot(timeSlotService.getEntityById(request.getTimeSlotId()));
        }
        response.setSectionNumber(request.getNumber());
        response.setInstructor(request.getInstructor());

        Lecture updated = lectureRepository.save(response);
        return LectureMapper.mapToResponse(updated);
    }
}