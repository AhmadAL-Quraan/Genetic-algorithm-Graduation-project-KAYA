package com.kaya.dataManager.dataGenerator;

import com.kaya.dto.response.TimeTableResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/manual-entry-generator")
@RequiredArgsConstructor
public class DataGeneratorController {

    private final DataGeneratorService DataGeneratorService;

    @PostMapping
    public ResponseEntity<TimeTableResponse> create() {
        return ResponseEntity.ok(DataGeneratorService.create());
    }
}