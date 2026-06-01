package com.kaya.service;

import com.kaya.dto.request.DepartmentRequest;
import com.kaya.dto.response.DepartmentResponse;
import com.kaya.model.Department;
import com.kaya.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public List<DepartmentResponse> getAll() {
        return departmentRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public DepartmentResponse getById(Long id) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found"));
        return mapToResponse(dept);
    }

    public Department getEntityById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found"));
    }

    public DepartmentResponse create(DepartmentRequest request) {
        Department dept = new Department();
        return save(request, dept);
    }

    public DepartmentResponse update(Long id, DepartmentRequest request) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found"));
        return save(request, dept);
    }

    public void delete(Long id) {
        if (!departmentRepository.existsById(id)) {
            throw new RuntimeException("Department not found");
        }
        departmentRepository.deleteById(id);
    }

    public void deleteAll() {
        departmentRepository.deleteAll();
    }

    private DepartmentResponse save(DepartmentRequest request, Department dept) {
        dept.setName(request.getName().toUpperCase());
        dept.setCode(request.getCode().toUpperCase());
        return mapToResponse(departmentRepository.save(dept));
    }

    private DepartmentResponse mapToResponse(Department dept) {
        return new DepartmentResponse(dept.getId(), dept.getName(), dept.getCode());
    }
}
