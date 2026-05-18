package com.kaya.dataManager.excelHandler.exportHandler;

import com.kaya.model.Course;
import com.kaya.model.TimeTable;
import com.kaya.repository.CourseRepository;
import com.kaya.repository.TimeTableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExportService {

    private final TimeTableRepository timeTableRepository;
    private final CourseRepository courseRepository;
    private final ExportBuilder exportBuilder;

    public ResponseEntity<byte[]> exportLatest() throws Exception {
        TimeTable tt = timeTableRepository.findAll().stream()
                .max(Comparator.comparingLong(t -> t.getId() == null ? 0L : t.getId()))
                .orElseThrow(() -> new IllegalStateException("No timetables found"));

        return exportBuilder.buildTimetableExport(tt);
    }

    public ResponseEntity<byte[]> exportTimetable(Long id) throws Exception {
        TimeTable tt = timeTableRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Timetable not found: " + id));

        return exportBuilder.buildTimetableExport(tt);
    }

    public ResponseEntity<byte[]> exportCourses() throws Exception {
        List<Course> courses = courseRepository.findAll();
        return exportBuilder.buildCoursesExport(courses);
    }
}