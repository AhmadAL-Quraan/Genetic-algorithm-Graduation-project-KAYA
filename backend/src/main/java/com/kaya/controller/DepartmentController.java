package com.kaya.controller;

import com.kaya.dto.request.DepartmentRequest;
import com.kaya.dto.response.DepartmentResponse;
import com.kaya.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    public List<DepartmentResponse> getAll() {
        return departmentService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepartmentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(departmentService.getById(id));
    }

    @PostMapping
    public ResponseEntity<DepartmentResponse> create(@Valid @RequestBody DepartmentRequest request) {
        return ResponseEntity.ok(departmentService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DepartmentResponse> update(@PathVariable Long id, @Valid @RequestBody DepartmentRequest request) {
        return ResponseEntity.ok(departmentService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        departmentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAll() {
        departmentService.deleteAll();
        return ResponseEntity.noContent().build();
    }
}
