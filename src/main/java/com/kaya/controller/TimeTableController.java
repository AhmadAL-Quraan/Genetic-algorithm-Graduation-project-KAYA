package com.kaya.controller;

<<<<<<< HEAD
import com.kaya.dto.request.TimeTableRequest;
import com.kaya.dto.response.TimeTableResponse;
import com.kaya.service.TimeTableService;
=======
import com.kaya.dto.response.TimeTableResponse;
import com.kaya.model.TimeTable;
>>>>>>> 2f1def42acd0dd54877d40fc787cdfb45bbf7ddf
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
<<<<<<< HEAD
    public ResponseEntity<TimeTableResponse> create(@RequestBody TimeTableRequest request) {
=======
    public ResponseEntity<TimeTableResponse> create(@RequestBody TimeTable request) {
>>>>>>> 2f1def42acd0dd54877d40fc787cdfb45bbf7ddf
        return ResponseEntity.ok(timeTableService.create(request));
    }

    @PutMapping("/{id}")
<<<<<<< HEAD
    public ResponseEntity<TimeTableResponse> update(@PathVariable Long id, @RequestBody TimeTableRequest request) {
=======
    public ResponseEntity<TimeTableResponse> update(@PathVariable Long id, @RequestBody TimeTable request) {
>>>>>>> 2f1def42acd0dd54877d40fc787cdfb45bbf7ddf
        return ResponseEntity.ok(timeTableService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        timeTableService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
