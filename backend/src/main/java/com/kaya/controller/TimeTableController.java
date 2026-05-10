package com.kaya.controller;

import com.kaya.dto.request.TimeTableRequest;
import com.kaya.dto.response.ProgressResponse;
import com.kaya.dto.response.TimeTableResponse;
import com.kaya.service.TimeTableService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/time-table")
@RequiredArgsConstructor
public class TimeTableController {

    private final TimeTableService timeTableService;

    @GetMapping
    public List<TimeTableResponse> getAll() {
        return timeTableService.getAll();
    }

    @GetMapping("/progress")
    public ResponseEntity<ProgressResponse> getProgress() {
        ProgressResponse progress = timeTableService.getProgress();
        if (progress == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TimeTableResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(timeTableService.getById(id));
    }

    @PostMapping
    public ResponseEntity<TimeTableResponse> create(@RequestBody TimeTableRequest request) {
        return ResponseEntity.ok(timeTableService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TimeTableResponse> update(@PathVariable Long id, @RequestBody TimeTableRequest request) {
        return ResponseEntity.ok(timeTableService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        timeTableService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAll() {
        timeTableService.deleteAll();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/generate")
    public ResponseEntity<TimeTableResponse> generate(@RequestBody(required = false) Map<String, Object> configMap) {
        return ResponseEntity.ok(timeTableService.generate(configMap));
    }

    @PostMapping("/cancel")
    public ResponseEntity<Void> cancel() {
        timeTableService.cancelGeneration();
        return ResponseEntity.ok().build();
    }
}
