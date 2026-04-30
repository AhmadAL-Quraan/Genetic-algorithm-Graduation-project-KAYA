package com.kaya.service;

import com.kaya.dto.request.CourseRequest;
import com.kaya.dto.response.CourseResponse;
import com.kaya.mapper.CourseMapper;
import com.kaya.model.*;
import com.kaya.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.HashSet;
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
        course.setMajors(req.getMajors());
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

        // Determine if we have any scheduling info
        boolean hasTeacher   = teacher != null || (req.getInstructor() != null && !req.getInstructor().isBlank());
        boolean hasRoom      = req.getRoomId() != null
                || (req.getBuilding() != null && !req.getBuilding().isBlank()
                    && req.getRoomNumber() != null && !req.getRoomNumber().isBlank());
        boolean hasTimeSlot  = req.getTimeSlotId() != null
                || (req.getStartTime() != null && req.getEndTime() != null
                    && req.getDays() != null && !req.getDays().isEmpty());

        if (!hasTeacher && !hasRoom && !hasTimeSlot) {
            return CourseMapper.mapToResponse(saved, null);
        }

        // Resolve room
        Room room = null;
        if (req.getRoomId() != null) {
            room = roomRepository.findById(req.getRoomId()).orElse(null);
        } else if (req.getBuilding() != null && !req.getBuilding().isBlank()
                && req.getRoomNumber() != null && !req.getRoomNumber().isBlank()) {
            final String bld = req.getBuilding().trim();
            final String num = req.getRoomNumber().trim();
            room = roomRepository.findAll().stream()
                    .filter(r -> bld.equals(r.getBuilding()) && num.equals(r.getRoomNumber()))
                    .findFirst()
                    .orElseGet(() -> roomRepository.save(new Room(bld, num,
                            req.getRoomType() != null ? req.getRoomType() : req.getRoomGroups())));
        }

        // Resolve time slot
        TimeSlot slot = null;
        if (req.getTimeSlotId() != null) {
            slot = timeSlotRepository.findById(req.getTimeSlotId()).orElse(null);
        } else if (req.getStartTime() != null && req.getEndTime() != null
                && req.getDays() != null && !req.getDays().isEmpty()) {
            LocalTime start = LocalTime.parse(req.getStartTime());
            LocalTime end   = LocalTime.parse(req.getEndTime());
            var days   = new HashSet<>(req.getDays());
            var method = req.getTimeGroups();
            slot = timeSlotRepository.findAll().stream()
                    .filter(ts -> ts.getStartTime().equals(start) && ts.getEndTime().equals(end)
                            && ts.getDays().equals(days) && ts.getTeachingMethod() == method)
                    .findFirst()
                    .orElseGet(() -> timeSlotRepository.save(new TimeSlot(start, end, days, method)));
        }

        List<Lecture> existing = lectureRepository.findAllByCourse_Id(saved.getId());
        Lecture lecture = existing.isEmpty() ? new Lecture() : existing.get(0);
        lecture.setCourse(saved);
        lecture.setTeacher(teacher);
        lecture.setInstructor(teacher != null ? teacher.getName()
                : (req.getInstructor() != null ? req.getInstructor().trim() : null));
        lecture.setSectionNumber(req.getSectionNumber() != null ? req.getSectionNumber() : 1L);
        lecture.setRoom(room);
        lecture.setTimeSlot(slot);
        Lecture savedLecture = lectureRepository.save(lecture);

        return CourseMapper.mapToResponse(saved, savedLecture);
    }
}
