package com.kaya.controller;

import com.kaya.dto.request.FitnessReportRequest;
import com.kaya.dto.response.FitnessReportResponse;
import com.kaya.service.FitnessReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/fitnessReports")
@RequiredArgsConstructor
public class FitnessReportController {

    private final FitnessReportService fitnessReportService;

    @GetMapping
    public List<FitnessReportResponse> getAll() {
        return fitnessReportService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<FitnessReportResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(fitnessReportService.getById(id));
    }

    @PostMapping
    public ResponseEntity<FitnessReportResponse> create(@Valid @RequestBody FitnessReportRequest request) {
        return ResponseEntity.ok(fitnessReportService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FitnessReportResponse> update(@PathVariable Long id, @Valid @RequestBody FitnessReportRequest request) {
        return ResponseEntity.ok(fitnessReportService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        fitnessReportService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAll() {
        fitnessReportService.deleteAll();
        return ResponseEntity.noContent().build();
    }
}
