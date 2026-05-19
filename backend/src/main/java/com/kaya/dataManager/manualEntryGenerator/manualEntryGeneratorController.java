package com.kaya.dataManager.manualEntryGenerator;

import com.kaya.dto.response.TimeTableResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/manual-entry-generator")
@RequiredArgsConstructor
public class manualEntryGeneratorController {

    private final manualEntryGeneratorService manualEntryGeneratorService;

    @PostMapping
    public ResponseEntity<TimeTableResponse> create() {
        return ResponseEntity.ok(manualEntryGeneratorService.create());
    }
}