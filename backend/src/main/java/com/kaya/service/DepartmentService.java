package com.kaya.service;

import com.kaya.dto.request.DepartmentRequest;
import com.kaya.dto.response.DepartmentResponse;
import com.kaya.model.Course;
import com.kaya.model.Department;
import com.kaya.model.Teacher;
import com.kaya.repository.CourseRepository;
import com.kaya.repository.DepartmentRepository;
import com.kaya.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository repo;
    private final TeacherRepository teacherRepository;
    private final CourseRepository courseRepository;

    public List<DepartmentResponse> getAll() {
        return repo.findAll().stream().map(this::toResponse).toList();
    }

    public DepartmentResponse getById(Long id) {
        return toResponse(repo.findById(id).orElseThrow(() -> new RuntimeException("Department not found")));
    }

    public DepartmentResponse create(DepartmentRequest req) {
        Department d = new Department();
        d.setName(req.getName());
        d.setCode(req.getCode().toUpperCase());
        return toResponse(repo.save(d));
    }

    public DepartmentResponse update(Long id, DepartmentRequest req) {
        Department d = repo.findById(id).orElseThrow(() -> new RuntimeException("Department not found"));
        d.setName(req.getName());
        d.setCode(req.getCode().toUpperCase());
        return toResponse(repo.save(d));
    }

    @Transactional
    public void delete(Long id) {
        if (!repo.existsById(id)) throw new RuntimeException("Department not found");
        List<Teacher> teachers = teacherRepository.findAll().stream()
                .filter(t -> t.getDepartment() != null && id.equals(t.getDepartment().getId()))
                .toList();
        teachers.forEach(t -> t.setDepartment(null));
        teacherRepository.saveAll(teachers);
        List<Course> courses = courseRepository.findAll().stream()
                .filter(c -> c.getDepartment() != null && id.equals(c.getDepartment().getId()))
                .toList();
        courses.forEach(c -> c.setDepartment(null));
        courseRepository.saveAll(courses);
        repo.deleteById(id);
    }

    @Transactional
    public void deleteAll() {
        teacherRepository.findAll().forEach(t -> t.setDepartment(null));
        teacherRepository.saveAll(teacherRepository.findAll());
        courseRepository.findAll().forEach(c -> c.setDepartment(null));
        courseRepository.saveAll(courseRepository.findAll());
        repo.deleteAll();
    }

    public DepartmentResponse toResponse(Department d) {
        if (d == null) return null;
        return new DepartmentResponse(d.getId(), d.getName(), d.getCode());
    }
}
