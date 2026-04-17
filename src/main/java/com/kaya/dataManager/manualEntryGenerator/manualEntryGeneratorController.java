package com.kaya.dataManager.manualEntryGenerator;

import com.kaya.dto.response.LectureResponse;
import com.kaya.model.Lecture;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequestMapping("/manual-entry-generator")
@RequiredArgsConstructor
public class manualEntryGeneratorController {

    private final manualEntryGeneratorService manualEntryGeneratorService;

    @PostMapping
    public ResponseEntity<ArrayList<Lecture>> create() {
        return ResponseEntity.ok(manualEntryGeneratorService.create());
    }
}