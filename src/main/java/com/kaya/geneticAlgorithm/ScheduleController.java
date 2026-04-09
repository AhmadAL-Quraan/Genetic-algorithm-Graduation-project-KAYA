package com.kaya.geneticAlgorithm;

import com.kaya.dto.response.TimeTableResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @PostMapping("/generate")
    public ResponseEntity<TimeTableResponse> generate(@RequestBody ScheduleRequestDTO params) {
        return ResponseEntity.ok(scheduleService.generateSchedule(params));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TimeTableResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(scheduleService.getById(id));
    }
}