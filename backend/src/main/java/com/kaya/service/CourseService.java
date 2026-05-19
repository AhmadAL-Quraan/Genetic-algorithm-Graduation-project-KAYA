package com.kaya.service;

import com.kaya.dto.request.CourseRequest;
import com.kaya.dto.response.CourseResponse;
import com.kaya.dto.mapper.CourseMapper;
import com.kaya.model.Course;
import com.kaya.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;

    public List<CourseResponse> getAll() {
        return courseRepository.findAll()
                .stream()
                .map(CourseMapper::mapToResponse)
                .toList();
    }

    public CourseResponse getById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        return CourseMapper.mapToResponse(course);
    }

    public Course getEntityById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));
    }

    public CourseResponse create(CourseRequest request) {
        Course response = new Course();
        return saveCourse(request, response);
    }

    public CourseResponse update(Long id, CourseRequest request) {
        Course response = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        return saveCourse(request, response);
    }

    public void delete(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new RuntimeException("Course not found");
        }
        courseRepository.deleteById(id);
    }

    public void deleteAll() {
        courseRepository.deleteAll();
    }

    // --- Helper methods --- //

    private CourseResponse saveCourse(CourseRequest request, Course response) {

        response.setCourseSymbol(request.getCourseSymbol());
        response.setCourseNumber(request.getCourseNumber());
        response.setTeachingMethod(request.getTimeGroups());
        response.setRequiredRoomType(request.getRoomGroups());

        Course updated = courseRepository.save(response);
        return CourseMapper.mapToResponse(updated);
    }
}
