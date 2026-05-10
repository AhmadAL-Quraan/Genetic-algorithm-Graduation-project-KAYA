package com.kaya.service;

import com.kaya.dto.request.TeacherRequest;
import com.kaya.dto.response.DepartmentResponse;
import com.kaya.dto.response.TeacherResponse;
import com.kaya.model.Department;
import com.kaya.model.Teacher;
import com.kaya.repository.CourseRepository;
import com.kaya.repository.LectureRepository;
import com.kaya.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final DepartmentService departmentService;
    private final LectureRepository lectureRepository;
    private final CourseRepository courseRepository;

    public List<TeacherResponse> getAll() {
        return teacherRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public TeacherResponse getById(Long id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        return mapToResponse(teacher);
    }

    public TeacherResponse create(TeacherRequest request) {
        Teacher teacher = new Teacher();
        return save(request, teacher);
    }

    public TeacherResponse update(Long id, TeacherRequest request) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        return save(request, teacher);
    }

    @Transactional
    public void delete(Long id) {
        if (!teacherRepository.existsById(id)) {
            throw new RuntimeException("Teacher not found");
        }
        courseRepository.detachTeacher(id);
        lectureRepository.detachTeacher(id);
        teacherRepository.deleteById(id);
    }

    @Transactional
    public void deleteAll() {
        courseRepository.detachAllTeachers();
        lectureRepository.detachAllTeachers();
        teacherRepository.deleteAll();
    }

    private TeacherResponse save(TeacherRequest request, Teacher teacher) {
        teacher.setName(request.getName());
        teacher.setEmail(request.getEmail());
        if (request.getDepartmentId() != null) {
            Department dept = departmentService.getEntityById(request.getDepartmentId());
            teacher.setDepartment(dept);
        } else {
            teacher.setDepartment(null);
        }
        return mapToResponse(teacherRepository.save(teacher));
    }

    private TeacherResponse mapToResponse(Teacher teacher) {
        DepartmentResponse deptResp = null;
        if (teacher.getDepartment() != null) {
            Department d = teacher.getDepartment();
            deptResp = new DepartmentResponse(d.getId(), d.getName(), d.getCode());
        }
        return new TeacherResponse(teacher.getId(), teacher.getName(), teacher.getEmail(), deptResp);
    }
}
