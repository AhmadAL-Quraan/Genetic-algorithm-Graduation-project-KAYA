package com.kaya.dataManager.manualEntryHandler;

import com.kaya.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ManualEntryService {

    private final ManualEntryRepository manualEntryRepository;
    private final CourseRepository courseRepository;

    public List<ManualEntryResponse> getAll() {
        return manualEntryRepository.findAll()
                .stream()
                .map(ManualEntryMapper::mapToDTO)
                .toList();
    }

    public ManualEntryResponse getById(Long id) {
        ManualEntry dataManager = manualEntryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("DataManager not found"));

        return ManualEntryMapper.mapToDTO(dataManager);
    }

    public ManualEntryResponse create(ManualEntryRequest request) {
        ManualEntry response = new ManualEntry();
        return saveDataManager(request, response);
    }

    public List<ManualEntryResponse> createBulk(List<ManualEntryRequest> request) {
        List<ManualEntryResponse> l = new ArrayList<>();

        for (ManualEntryRequest manualEntryRequest : request) {
            ManualEntry response = new ManualEntry();
            l.add(saveDataManager(manualEntryRequest, response));
        }
        return l;
    }

    public ManualEntryResponse update(Long id, ManualEntryRequest request) {
        ManualEntry response = manualEntryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("DataManager not found"));

        return saveDataManager(request, response);
    }

    public void delete(Long id) {
        if (!manualEntryRepository.existsById(id)) {
            throw new RuntimeException("DataManager not found");
        }
        manualEntryRepository.deleteById(id);
    }

    public void deleteAll() {
        manualEntryRepository.deleteAll();
    }

    // --- Helper methods --- //

    private ManualEntryResponse saveDataManager(ManualEntryRequest request, ManualEntry response) {

        // Save Instructor and CourseId
        response.setInstructorId(request.getInstructorId());
        response.setCourseId(request.getCourseId());

        ManualEntry updated = manualEntryRepository.save(response);
        return ManualEntryMapper.mapToDTO(updated);
    }
}
