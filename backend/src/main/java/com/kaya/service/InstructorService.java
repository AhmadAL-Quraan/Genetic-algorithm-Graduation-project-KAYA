package com.kaya.service;

import com.kaya.dto.mapper.InstructorMapper;
import com.kaya.dto.request.InstructorRequest;
import com.kaya.dto.response.InstructorResponse;
import com.kaya.model.Instructor;
import com.kaya.repository.DepartmentRepository;
import com.kaya.repository.InstructorRepository;
import com.kaya.repository.LectureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InstructorService {

    private final InstructorRepository instructorRepository;
    private final LectureRepository lectureRepository;
    private final DepartmentRepository departmentRepository;

    public List<InstructorResponse> getAll() {
        return instructorRepository.findAll()
                .stream()
                .map(InstructorMapper::mapToResponse)
                .toList();
    }

    public InstructorResponse getById(Long id) {
        Instructor instructor = instructorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Instructor not found"));
        return InstructorMapper.mapToResponse(instructor);
    }

    public Instructor getEntityById(Long id) {
        return instructorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Instructor not found"));
    }

    public InstructorResponse create(InstructorRequest request) {
        Instructor instructor = new Instructor();
        return save(request, instructor);
    }

    public InstructorResponse update(Long id, InstructorRequest request) {
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
        lectureRepository.detachInstructor(id);
        instructorRepository.deleteById(id);
    }

    @Transactional
    public void deleteAll() {
        lectureRepository.clearAllConflictingLectureRefs();
        lectureRepository.detachAllInstructors();
        instructorRepository.deleteAll();
    }

    private InstructorResponse save(InstructorRequest request, Instructor instructor) {
        instructor.setInstructorName(request.getName());
        instructor.setEmail(request.getEmail());
        if (request.getDepartmentId() != null) {
            instructor.setDepartment(
                    departmentRepository.findById(request.getDepartmentId()).orElse(null)
            );
        } else {
            instructor.setDepartment(null);
        }
        return InstructorMapper.mapToResponse(instructorRepository.save(instructor));
    }
}
