package com.kaya.controller;

import com.kaya.dto.request.TeacherRequest;
import com.kaya.dto.response.TeacherResponse;
import com.kaya.service.TeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teachers")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;

    @GetMapping
    public List<TeacherResponse> getAll() {
        return teacherService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeacherResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(teacherService.getById(id));
    }

    @PostMapping
    public ResponseEntity<TeacherResponse> create(@RequestBody TeacherRequest request) {
        return ResponseEntity.ok(teacherService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TeacherResponse> update(@PathVariable Long id, @RequestBody TeacherRequest request) {
        return ResponseEntity.ok(teacherService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        teacherService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAll() {
        teacherService.deleteAll();
        return ResponseEntity.noContent().build();
    }
}
