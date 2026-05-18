package com.kaya.service;

import com.kaya.dto.request.FitnessReportRequest;
import com.kaya.dto.response.FitnessReportResponse;
import com.kaya.dto.mapper.FitnessReportMapper;
import com.kaya.model.FitnessReport;
import com.kaya.repository.FitnessReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FitnessReportService {

    private final FitnessReportRepository fitnessReportRepository;

    public List<FitnessReportResponse> getAll() {
        return fitnessReportRepository.findAll()
                .stream()
                .map(FitnessReportMapper::mapToResponse)
                .toList();
    }

    public FitnessReportResponse getById(Long id) {
        FitnessReport fitnessReport = fitnessReportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FitnessReport not found"));

        return FitnessReportMapper.mapToResponse(fitnessReport);
    }

    public FitnessReport getEntityById(Long id) {
        return fitnessReportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FitnessReport not found"));
    }

    public FitnessReportResponse create(FitnessReportRequest request) {
        FitnessReport response = new FitnessReport();
        return saveFitnessReport(request, response);
    }

    public FitnessReportResponse update(Long id, FitnessReportRequest request) {
        FitnessReport response = fitnessReportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FitnessReport not found"));

        return saveFitnessReport(request, response);
    }

    public void delete(Long id) {
        if (!fitnessReportRepository.existsById(id)) {
            throw new RuntimeException("FitnessReport not found");
        }
        fitnessReportRepository.deleteById(id);
    }

    public void deleteAll() {
        fitnessReportRepository.deleteAll();
    }

    // --- Helper methods --- //

    private FitnessReportResponse saveFitnessReport(FitnessReportRequest request, FitnessReport response) {

        response.setInstructorConflicts(request.getInstructorConflicts());
        response.setRoomConflicts(request.getRoomConflicts());
        response.setStudentConflicts(request.getStudentConflicts());
        response.setTotalPenalty(request.getTotalPenalty());
        response.setConflictingLectures(request.getConflictingLectures());

        FitnessReport updated = fitnessReportRepository.save(response);
        return FitnessReportMapper.mapToResponse(updated);
    }
}
