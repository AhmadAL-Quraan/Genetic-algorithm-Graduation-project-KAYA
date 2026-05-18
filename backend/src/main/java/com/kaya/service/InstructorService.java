package com.kaya.service;

import com.kaya.dto.mapper.InstructorMapper;
import com.kaya.dto.request.InstructorRequest;
import com.kaya.dto.response.InstructorResponse;
import com.kaya.model.Instructor;
import com.kaya.repository.InstructorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InstructorService {

    private final InstructorRepository instructorRepository;

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
        Instructor response = new Instructor();
        return saveInstructor(request, response);
    }

    public InstructorResponse update(Long id, InstructorRequest request) {
        Instructor response = instructorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Instructor not found"));

        return saveInstructor(request, response);
    }

    public void delete(Long id) {
        if (!instructorRepository.existsById(id)) {
            throw new RuntimeException("Instructor not found");
        }
        instructorRepository.deleteById(id);
    }

    public void deleteAll() {
        instructorRepository.deleteAll();
    }

    // --- Helper methods --- //

    private InstructorResponse saveInstructor(InstructorRequest request, Instructor response) {

        response.setInstructorName(request.getInstructorName());

        Instructor updated = instructorRepository.save(response);
        return InstructorMapper.mapToResponse(updated);
    }
}
