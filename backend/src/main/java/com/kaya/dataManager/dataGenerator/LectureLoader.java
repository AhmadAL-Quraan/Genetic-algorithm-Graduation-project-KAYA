package com.kaya.dataManager.dataGenerator;

import com.kaya.dataManager.manualEntryHandler.ManualEntry;
import com.kaya.dataManager.manualEntryHandler.ManualEntryRepository;
import com.kaya.model.Lecture;
import com.kaya.repository.LectureRepository;
import com.kaya.service.CourseService;
import com.kaya.service.InstructorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class LectureLoader {

    private final LectureRepository lectureRepository;
    private final ManualEntryRepository manualEntryRepository;
    private final CourseService courseService;
    private final InstructorService instructorService;

    public List<Lecture> load() {
        List<Lecture> lectures = lectureRepository.findByTimetableIdIsNull()
                .stream()
                .filter(l -> l.getCourse() != null)
                .toList();

        if (lectures.isEmpty()) {
            lectures = buildFromManualEntries();
        }

        if (lectures.isEmpty()) {
            throw new IllegalStateException(
                    "No lecture sections found. Add lecture sections on the Lectures page (or manual entries) before generating.");
        }

        return lectures;
    }

    private List<Lecture> buildFromManualEntries() {
        List<ManualEntry> manualEntries = manualEntryRepository.findAll();
        List<Lecture> result = new ArrayList<>();

        for (ManualEntry entry : manualEntries) {
            if (entry.getCourseId() == null) continue;
            try {
                Lecture l = new Lecture();
                l.setId(null);
                l.setCourse(courseService.getEntityById(entry.getCourseId()));
                l.setInstructor(instructorService.getEntityById(entry.getInstructorId()));
                result.add(l);
            } catch (RuntimeException ignored) {
                // Course may have been deleted; skip this entry
            }
        }

        return result;
    }
}