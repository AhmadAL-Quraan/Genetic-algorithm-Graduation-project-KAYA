package com.kaya.dataManager.excelHandler.exportHandler;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/export")
@RequiredArgsConstructor
public class ExportController {

    private final ExportService exportService;

    @GetMapping("/schedule/latest")
    public ResponseEntity<byte[]> exportLatest() throws Exception {
        return exportService.exportLatest();
    }

    @GetMapping("/schedule/{id}")
    public ResponseEntity<byte[]> exportTimetable(@PathVariable Long id) throws Exception {
        return exportService.exportTimetable(id);
    }

    @GetMapping("/schedule")
    public ResponseEntity<byte[]> exportCourses() throws Exception {
        return exportService.exportCourses();
    }
}