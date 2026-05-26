package com.kaya.service;

import com.kaya.dto.request.CourseRequest;
import com.kaya.dto.response.CourseResponse;
import com.kaya.dto.mapper.CourseMapper;
import com.kaya.model.Course;
import com.kaya.repository.CourseRepository;
import com.kaya.repository.LectureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final LectureRepository lectureRepository;

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
        Course course = new Course();
        return saveCourse(request, course);
    }

    public CourseResponse update(Long id, CourseRequest request) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        return saveCourse(request, course);
    }

    @Transactional
    public void delete(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new RuntimeException("Course not found");
        }
        lectureRepository.detachCourse(id);
        courseRepository.deleteById(id);
        lectureRepository.deleteOrphanedTemplateLectures();
    }

    @Transactional
    public void deleteAll() {
        lectureRepository.detachAllCourses();
        courseRepository.deleteAll();
        lectureRepository.deleteOrphanedTemplateLectures();
    }

    private CourseResponse saveCourse(CourseRequest request, Course course) {
        course.setCourseSymbol(request.getCourseSymbol().toUpperCase());
        course.setCourseNumber(request.getCourseNumber());
        course.setTeachingMethod(request.getTimeGroups());
        course.setRequiredRoomType(request.getRoomGroups());
        Course updated = courseRepository.save(course);
        return CourseMapper.mapToResponse(updated);
    }
}
