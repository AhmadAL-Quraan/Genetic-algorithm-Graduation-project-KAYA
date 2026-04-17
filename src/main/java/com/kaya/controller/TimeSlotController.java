package com.kaya.controller;


import com.kaya.dto.request.TimeSlotRequest;
import com.kaya.dto.response.TimeSlotResponse;
import com.kaya.model.TimeSlot;
import com.kaya.service.TimeSlotService;
import jakarta.validation.Valid;
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
    public ResponseEntity<TimeSlotResponse> create(@RequestBody TimeSlotRequest request) {
        return ResponseEntity.ok(timeSlotService.create(request));
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<TimeSlotResponse> > createBulk(@Valid @RequestBody List<TimeSlotRequest> request) {
        return ResponseEntity.ok(timeSlotService.createBulk(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TimeSlotResponse> update(@PathVariable Long id, @RequestBody TimeSlotRequest request) {
        return ResponseEntity.ok(timeSlotService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        timeSlotService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
