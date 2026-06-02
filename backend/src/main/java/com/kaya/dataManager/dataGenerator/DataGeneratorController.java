package com.kaya.dataManager.dataGenerator;

import com.kaya.dto.response.ProgressResponse;
import com.kaya.dto.response.TimeTableResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/generator")
@RequiredArgsConstructor
public class DataGeneratorController {

    private final DataGeneratorService dataGeneratorService;

    @PostMapping("/generate")
    public ResponseEntity<TimeTableResponse> generate(@RequestBody(required = false) Map<String, Object> configMap) {
        return ResponseEntity.ok(dataGeneratorService.generate(configMap));
    }

    @GetMapping("/progress")
    public ResponseEntity<ProgressResponse> getProgress() {
        ProgressResponse progress = dataGeneratorService.getProgress();
        if (progress == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(progress);
    }

    @PostMapping("/cancel")
    public ResponseEntity<Void> cancel() {
        dataGeneratorService.cancelGeneration();
        return ResponseEntity.ok().build();
    }
}