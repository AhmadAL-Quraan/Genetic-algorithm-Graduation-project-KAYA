package com.kaya.service;

import com.kaya.dto.request.LectureRequest;
import com.kaya.dto.response.LectureResponse;
import com.kaya.mapper.LectureMapper;
import com.kaya.model.Lecture;
import com.kaya.repository.CourseRepository;
import com.kaya.repository.LectureRepository;
import com.kaya.repository.RoomRepository;
import com.kaya.repository.TimeSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LectureService {

    private final LectureRepository lectureRepository;
    private final CourseRepository courseRepository;
    private final RoomRepository roomRepository;
    private final TimeSlotRepository timeSlotRepository;

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

    // --- Helper methods --- //

    private LectureResponse saveLecture(LectureRequest request, Lecture response) {

        response.setCourse(courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found: " + request.getCourseId())));
        response.setSectionNumber(request.getNumber() != null ? request.getNumber() : 1L);
        response.setInstructor(request.getInstructor());
        response.setRoom(request.getRoomId() != null
                ? roomRepository.findById(request.getRoomId()).orElseThrow(() -> new RuntimeException("Room not found")) : null);
        response.setTimeSlot(request.getTimeSlotId() != null
                ? timeSlotRepository.findById(request.getTimeSlotId()).orElseThrow(() -> new RuntimeException("TimeSlot not found")) : null);

        Lecture updated = lectureRepository.save(response);
        return LectureMapper.mapToResponse(updated);
    }
}