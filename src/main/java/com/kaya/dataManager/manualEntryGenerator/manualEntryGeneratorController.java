package com.kaya.dataManager.manualEntryGenerator;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/manual-entry-generator")
@RequiredArgsConstructor
public class manualEntryGeneratorController {

    private final manualEntryGeneratorService manualEntryGeneratorService;

    @PostMapping
    public ResponseEntity<Void> create() {
        manualEntryGeneratorService.create();
        return ResponseEntity.ok().build();
    }
}