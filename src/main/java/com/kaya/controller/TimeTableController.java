package com.kaya.controller;

import com.kaya.dto.request.TimeTableRequest;
import com.kaya.dto.response.TimeTableResponse;
import com.kaya.service.TimeTableService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/time-table")
@RequiredArgsConstructor
public class TimeTableController {

    private final TimeTableService timeTableService;

    @GetMapping
    public List<TimeTableResponse> getAll() {
        return timeTableService.getAll();
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
}
