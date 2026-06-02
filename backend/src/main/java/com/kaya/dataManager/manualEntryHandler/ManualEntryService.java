package com.kaya.dataManager.manualEntryHandler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ManualEntryService {

    private final ManualEntryRepository manualEntryRepository;

    public List<ManualEntryResponse> getAll() {
        return manualEntryRepository.findAll()
                .stream()
                .map(ManualEntryMapper::mapToResponse)
                .toList();
    }

    public ManualEntryResponse getById(Long id) {
        ManualEntry manualEntry = manualEntryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ManualEntry not found"));
        return ManualEntryMapper.mapToResponse(manualEntry);
    }

    public ManualEntryResponse create(ManualEntryRequest request) {
        ManualEntry entry = new ManualEntry();
        return saveDataManager(request, entry);
    }

    public List<ManualEntryResponse> createBulk(List<ManualEntryRequest> requests) {
        return requests.stream()
                .map(r -> {
                    ManualEntry entry = new ManualEntry();
                    return saveDataManager(r, entry);
                })
                .toList();
    }

    public ManualEntryResponse update(Long id, ManualEntryRequest request) {
        ManualEntry entry = manualEntryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ManualEntry not found"));
        return saveDataManager(request, entry);
    }

    public void delete(Long id) {
        if (!manualEntryRepository.existsById(id)) {
            throw new RuntimeException("ManualEntry not found");
        }
        manualEntryRepository.deleteById(id);
    }

    public void deleteAll() {
        manualEntryRepository.deleteAll();
    }

    private ManualEntryResponse saveDataManager(ManualEntryRequest request, ManualEntry response) {

        response.setInstructorId(request.getInstructorId());
        response.setCourseId(request.getCourseId());
        ManualEntry updated = manualEntryRepository.save(response);
        return ManualEntryMapper.mapToResponse(updated);
    }
}