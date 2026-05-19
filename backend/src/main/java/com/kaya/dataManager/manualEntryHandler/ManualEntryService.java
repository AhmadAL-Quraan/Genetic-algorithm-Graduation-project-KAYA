package com.kaya.dataManager.manualEntryHandler;

import com.kaya.model.Course;
import com.kaya.model.Instructor;
import com.kaya.repository.CourseRepository;
import com.kaya.repository.InstructorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ManualEntryService {

    private final ManualEntryRepository manualEntryRepository;
    private final CourseRepository courseRepository;
    private final InstructorRepository instructorRepository;

    public List<ManualEntryResponse> getAll() {
        return manualEntryRepository.findAll()
                .stream()
                .map(ManualEntryMapper::mapToDTO)
                .toList();
    }

    public ManualEntryResponse getById(Long id) {
        ManualEntry manualEntry = manualEntryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ManualEntry not found"));
        return ManualEntryMapper.mapToDTO(manualEntry);
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

    private ManualEntryResponse saveDataManager(ManualEntryRequest request, ManualEntry entry) {
        Course course = new Course(
                request.getCourseSymbol(),
                request.getCourseNumber(),
                request.getRequiredRoomType(),
                request.getTeachingMethod()
        );
        courseRepository.save(course);

        Instructor instructor = null;
        if (request.getInstructorId() != null) {
            instructor = instructorRepository.findById(request.getInstructorId()).orElse(null);
        }
        entry.setInstructor(instructor);
        entry.setCourseId(course.getId());

        ManualEntry updated = manualEntryRepository.save(entry);
        return ManualEntryMapper.mapToDTO(updated);
    }
}
