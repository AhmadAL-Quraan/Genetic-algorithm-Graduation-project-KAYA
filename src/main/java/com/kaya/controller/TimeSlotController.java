package com.kaya.controller;


<<<<<<< HEAD
import com.kaya.dto.request.TimeSlotRequest;
import com.kaya.dto.response.TimeSlotResponse;
import com.kaya.model.TimeSlot;
import com.kaya.service.TimeSlotService;
=======
import com.kaya.dto.response.TimeSlotResponse;
import com.kaya.model.TimeSlot;
>>>>>>> 2f1def42acd0dd54877d40fc787cdfb45bbf7ddf
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/time-slots")
@RequiredArgsConstructor
public class TimeSlotController {

    private final TimeSlotService timeSlotService;

    @GetMapping
    public List<TimeSlotResponse> getAll() {
        return timeSlotService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TimeSlotResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(timeSlotService.getById(id));
    }

    @PostMapping
<<<<<<< HEAD
    public ResponseEntity<TimeSlotResponse> create(@RequestBody TimeSlotRequest request) {
=======
    public ResponseEntity<TimeSlotResponse> create(@RequestBody TimeSlot request) {
>>>>>>> 2f1def42acd0dd54877d40fc787cdfb45bbf7ddf
        return ResponseEntity.ok(timeSlotService.create(request));
    }

    @PutMapping("/{id}")
<<<<<<< HEAD
    public ResponseEntity<TimeSlotResponse> update(@PathVariable Long id, @RequestBody TimeSlotRequest request) {
=======
    public ResponseEntity<TimeSlotResponse> update(@PathVariable Long id, @RequestBody TimeSlot request) {
>>>>>>> 2f1def42acd0dd54877d40fc787cdfb45bbf7ddf
        return ResponseEntity.ok(timeSlotService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        timeSlotService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
