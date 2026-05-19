package com.kaya.service;

import com.kaya.dto.response.InstructorResponse;
import com.kaya.model.Instructor;
import com.kaya.repository.InstructorRepository;
import com.kaya.repository.LectureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.kaya.dto.request.TeacherRequest;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InstructorService {

    private final InstructorRepository instructorRepository;
    private final LectureRepository lectureRepository;

    public List<InstructorResponse> getAll() {
        return instructorRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public InstructorResponse getById(Long id) {
        Instructor instructor = instructorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Instructor not found"));
        return mapToResponse(instructor);
    }

    public Instructor getEntityById(Long id) {
        return instructorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Instructor not found"));
    }

    public InstructorResponse create(TeacherRequest request) {
        Instructor instructor = new Instructor();
        return save(request, instructor);
    }

    public InstructorResponse update(Long id, TeacherRequest request) {
        Instructor instructor = instructorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Instructor not found"));
        return save(request, instructor);
    }

    @Transactional
    public void delete(Long id) {
        if (!instructorRepository.existsById(id)) {
            throw new RuntimeException("Instructor not found");
        }
        lectureRepository.clearAllConflictingLectureRefs();
        lectureRepository.detachTeacher(id);
        instructorRepository.deleteById(id);
    }

    @Transactional
    public void deleteAll() {
        lectureRepository.clearAllConflictingLectureRefs();
        lectureRepository.detachAllTeachers();
        instructorRepository.deleteAll();
    }

    private InstructorResponse save(TeacherRequest request, Instructor instructor) {
        instructor.setInstructorName(request.getName());
        return mapToResponse(instructorRepository.save(instructor));
    }

    private InstructorResponse mapToResponse(Instructor instructor) {
        return new InstructorResponse(instructor.getId(), instructor.getInstructorName());
    }
}
