package com.kaya.controller;

import com.kaya.dto.response.InstructorResponse;
import com.kaya.service.InstructorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.kaya.dto.request.TeacherRequest;

import java.util.List;

@RestController
@RequestMapping("/teachers")
@RequiredArgsConstructor
public class InstructorController {

    private final InstructorService instructorService;

    @GetMapping
    public List<InstructorResponse> getAll() {
        return instructorService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<InstructorResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(instructorService.getById(id));
    }

    @PostMapping
    public ResponseEntity<InstructorResponse> create(@RequestBody TeacherRequest request) {
        return ResponseEntity.ok(instructorService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InstructorResponse> update(@PathVariable Long id, @RequestBody TeacherRequest request) {
        return ResponseEntity.ok(instructorService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        instructorService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAll() {
        instructorService.deleteAll();
        return ResponseEntity.noContent().build();
    }
}
