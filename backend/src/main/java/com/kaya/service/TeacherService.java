package com.kaya.service;

import com.kaya.dto.request.TeacherRequest;
import com.kaya.dto.response.TeacherResponse;
import com.kaya.model.Lecture;
import com.kaya.model.Teacher;
import com.kaya.repository.DepartmentRepository;
import com.kaya.repository.LectureRepository;
import com.kaya.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository repo;
    private final DepartmentRepository departmentRepository;
    private final DepartmentService departmentService;
    private final LectureRepository lectureRepository;

    public List<TeacherResponse> getAll() {
        return repo.findAll().stream().map(this::toResponse).toList();
    }

    public TeacherResponse getById(Long id) {
        return toResponse(repo.findById(id).orElseThrow(() -> new RuntimeException("Teacher not found")));
    }

    public TeacherResponse create(TeacherRequest req) {
        Teacher t = new Teacher();
        t.setName(req.getName());
        t.setEmail(req.getEmail());
        if (req.getDepartmentId() != null) {
            t.setDepartment(departmentRepository.findById(req.getDepartmentId()).orElse(null));
        }
        return toResponse(repo.save(t));
    }

    public TeacherResponse update(Long id, TeacherRequest req) {
        Teacher t = repo.findById(id).orElseThrow(() -> new RuntimeException("Teacher not found"));
        t.setName(req.getName());
        t.setEmail(req.getEmail());
        t.setDepartment(req.getDepartmentId() != null
                ? departmentRepository.findById(req.getDepartmentId()).orElse(null) : null);
        return toResponse(repo.save(t));
    }

    @Transactional
    public void delete(Long id) {
        if (!repo.existsById(id)) throw new RuntimeException("Teacher not found");
        List<Lecture> lectures = lectureRepository.findAll().stream()
                .filter(l -> l.getTeacher() != null && id.equals(l.getTeacher().getId()))
                .toList();
        lectures.forEach(l -> l.setTeacher(null));
        lectureRepository.saveAll(lectures);
        repo.deleteById(id);
    }

    @Transactional
    public void deleteAll() {
        lectureRepository.findAll().forEach(l -> l.setTeacher(null));
        lectureRepository.saveAll(lectureRepository.findAll());
        repo.deleteAll();
    }

    public TeacherResponse toResponse(Teacher t) {
        if (t == null) return null;
        return new TeacherResponse(
                t.getId(), t.getName(), t.getEmail(),
                t.getDepartment() != null ? departmentService.toResponse(t.getDepartment()) : null
        );
    }
}
