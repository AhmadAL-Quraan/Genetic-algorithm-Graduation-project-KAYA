package com.kaya.dataManager.manualEntry;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/manual-entry")
@RequiredArgsConstructor
public class ManualEntryController {

    private final ManualEntryService manualEntryService;

    @GetMapping
    public List<ManualEntryResponse> getAll() {
        return manualEntryService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ManualEntryResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(manualEntryService.getById(id));
    }

    @PostMapping
    public ResponseEntity<ManualEntryResponse> create(@Valid @RequestBody ManualEntryRequest request) {
        return ResponseEntity.ok(manualEntryService.create(request));
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<ManualEntryResponse> > createBulk(@Valid @RequestBody List<ManualEntryRequest> request) {
        return ResponseEntity.ok(manualEntryService.createBulk(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ManualEntryResponse> update(@PathVariable Long id, @Valid @RequestBody ManualEntryRequest request) {
        return ResponseEntity.ok(manualEntryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        manualEntryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAll() {
        manualEntryService.deleteAll();
        return ResponseEntity.noContent().build();
    }
}