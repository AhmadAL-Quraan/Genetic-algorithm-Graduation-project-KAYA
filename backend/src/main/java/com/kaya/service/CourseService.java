package com.kaya.service;

import com.kaya.dto.request.CourseRequest;
import com.kaya.dto.response.CourseResponse;
import com.kaya.mapper.CourseMapper;
import com.kaya.model.*;
import com.kaya.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository     courseRepository;
    private final LectureRepository    lectureRepository;
    private final RoomRepository       roomRepository;
    private final TimeSlotRepository   timeSlotRepository;
    private final TeacherRepository    teacherRepository;
    private final DepartmentRepository departmentRepository;

    public List<CourseResponse> getAll() {
        return courseRepository.findAll().stream().map(c -> {
            List<Lecture> lects = lectureRepository.findAllByCourse_Id(c.getId());
            return CourseMapper.mapToResponse(c, lects.isEmpty() ? null : lects.get(0));
        }).toList();
    }

    public CourseResponse getById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        List<Lecture> lects = lectureRepository.findAllByCourse_Id(id);
        return CourseMapper.mapToResponse(course, lects.isEmpty() ? null : lects.get(0));
    }

    @Transactional
    public CourseResponse create(CourseRequest request) { return saveCourse(request, new Course()); }

    @Transactional
    public CourseResponse update(Long id, CourseRequest request) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        return saveCourse(request, course);
    }

    @Transactional
    public void delete(Long id) {
        if (!courseRepository.existsById(id)) throw new RuntimeException("Course not found");
        lectureRepository.deleteAll(lectureRepository.findAllByCourse_Id(id));
        courseRepository.deleteById(id);
    }

    @Transactional
    public void deleteAll() {
        lectureRepository.deleteAll();
        courseRepository.deleteAll();
    }

    private CourseResponse saveCourse(CourseRequest req, Course course) {
        course.setCourseSymbol(req.getCourseSymbol());
        course.setCourseNumber(req.getCourseNumber());
        course.setMajors(req.getMajors() != null ? req.getMajors() : List.of());
        course.setRequiredRoomType(req.getRoomGroups());
        course.setTeachingMethod(req.getTimeGroups());

        if (req.getDepartmentId() != null) {
            departmentRepository.findById(req.getDepartmentId()).ifPresent(course::setDepartment);
        }

        Course saved = courseRepository.save(course);

        // Resolve teacher
        Teacher teacher = null;
        if (req.getTeacherId() != null) {
            teacher = teacherRepository.findById(req.getTeacherId()).orElse(null);
        }

        boolean hasTeacher = teacher != null
                || (req.getInstructor() != null && !req.getInstructor().isBlank());

        if (!hasTeacher) {
            return CourseMapper.mapToResponse(saved, null);
        }

        // Resolve existing lecture or create new
        List<Lecture> existing = lectureRepository.findAllByCourse_Id(saved.getId());
        Lecture lecture = existing.isEmpty() ? new Lecture() : existing.get(0);
        lecture.setCourse(saved);
        lecture.setTeacher(teacher);
        lecture.setInstructor(teacher != null ? teacher.getName() : req.getInstructor().trim());
        lecture.setSectionNumber(req.getSectionNumber() != null ? req.getSectionNumber() : 1L);

        if (req.getRoomId() != null) {
            roomRepository.findById(req.getRoomId()).ifPresent(lecture::setRoom);
        }
        if (req.getTimeSlotId() != null) {
            timeSlotRepository.findById(req.getTimeSlotId()).ifPresent(lecture::setTimeSlot);
        }

        Lecture savedLecture = lectureRepository.save(lecture);
        return CourseMapper.mapToResponse(saved, savedLecture);
    }
}
