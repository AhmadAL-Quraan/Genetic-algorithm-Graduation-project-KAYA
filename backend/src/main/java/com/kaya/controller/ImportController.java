package com.kaya.controller;

import com.kaya.service.ImportService;
import com.kaya.service.ImportService.ImportSummary;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/import")
public class ImportController {

    private final ImportService importService;

    public ImportController(ImportService importService) {
        this.importService = importService;
    }

    @PostMapping(path = "/excel", consumes = "multipart/form-data")
    public ResponseEntity<?> importExcel(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is required");
        }
        try {
            ImportSummary summary = importService.importExcel(file);
            return ResponseEntity.ok(summary);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Failed to read spreadsheet: " + e.getMessage());
        }
    }
}
