package com.kaya.service;

import com.kaya.dto.request.CourseRequest;
import com.kaya.dto.response.CourseResponse;
<<<<<<< HEAD
import com.kaya.mapper.CourseMapper;
=======
>>>>>>> 2f1def42acd0dd54877d40fc787cdfb45bbf7ddf
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
<<<<<<< HEAD
                .map(CourseMapper::mapToResponse)
=======
                .map(this::mapToDTO)
>>>>>>> 2f1def42acd0dd54877d40fc787cdfb45bbf7ddf
                .toList();
    }

    public CourseResponse getById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));

<<<<<<< HEAD
        return CourseMapper.mapToResponse(course);
=======
        return mapToDTO(course);
>>>>>>> 2f1def42acd0dd54877d40fc787cdfb45bbf7ddf
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

    // --- Helper methods --- //

<<<<<<< HEAD
=======
    // Maps the response into a DTO
    private CourseResponse mapToDTO(Course course) {
        return new CourseResponse(
                course.getId(),
                course.getCourseSymbol(),
                course.getCourseNumber(),
                course.getMajors(),
                course.getRoomGroups(),
                course.getTimeGroups()
        );
    }

>>>>>>> 2f1def42acd0dd54877d40fc787cdfb45bbf7ddf
    private CourseResponse saveCourse(CourseRequest request, Course response) {

        response.setCourseSymbol(request.getCourseSymbol());
        response.setCourseNumber(request.getCourseNumber());
        response.setMajors(request.getMajors());
        response.setTimeGroups(request.getTimeGroups());
        response.setRoomGroups(request.getRoomGroups());

        Course updated = courseRepository.save(response);
<<<<<<< HEAD
        return CourseMapper.mapToResponse(updated);
=======
        return mapToDTO(updated);
>>>>>>> 2f1def42acd0dd54877d40fc787cdfb45bbf7ddf
    }
}
