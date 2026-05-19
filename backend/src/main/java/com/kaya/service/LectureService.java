package com.kaya.service;

import com.kaya.dto.request.LectureRequest;
import com.kaya.dto.response.LectureResponse;
import com.kaya.dto.mapper.LectureMapper;
import com.kaya.model.Lecture;
import com.kaya.repository.InstructorRepository;
import com.kaya.repository.LectureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LectureService {

    private final LectureRepository lectureRepository;
    private final CourseService courseService;
    private final RoomService roomService;
    private final TimeSlotService timeSlotService;
    private final InstructorRepository teacherRepository;

    public List<LectureResponse> getAll() {
        return lectureRepository.findByTimetableIdIsNull()
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
        Lecture lecture = new Lecture();
        return saveLecture(request, lecture);
    }

    public LectureResponse update(Long id, LectureRequest request) {
        Lecture lecture = lectureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lecture not found"));
        return saveLecture(request, lecture);
    }

    @Transactional
    public void delete(Long id) {
        if (!lectureRepository.existsById(id)) {
            throw new RuntimeException("Lecture not found");
        }
        lectureRepository.clearConflictingLectureRef(id);
        lectureRepository.deleteById(id);
    }

    @Transactional
    public void deleteAll() {
        lectureRepository.clearAllConflictingLectureRefs();
        lectureRepository.deleteAllTemplateLectures();
    }

    private LectureResponse saveLecture(LectureRequest request, Lecture lecture) {
        if (request.getCourseId() != null) {
            lecture.setCourse(courseService.getEntityById(request.getCourseId()));
        }
        if (request.getTeacherId() != null) {
            lecture.setInstructor(teacherRepository.findById(request.getTeacherId()).orElse(null));
        } else {
            lecture.setInstructor(null);
        }
        if (request.getRoomId() != null) {
            lecture.setRoom(roomService.getEntityById(request.getRoomId()));
        }
        if (request.getTimeSlotId() != null) {
            lecture.setTimeSlot(timeSlotService.getEntityById(request.getTimeSlotId()));
        }
        Lecture updated = lectureRepository.save(lecture);
        return LectureMapper.mapToResponse(updated);
    }
}
