package com.kaya.service;

import com.kaya.dto.request.TimeTableRequest;
import com.kaya.dto.response.TimeTableResponse;
import com.kaya.mapper.TimeTableMapper;
import com.kaya.model.TimeTable;
import com.kaya.repository.LectureRepository;
import com.kaya.repository.TimeTableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TimeTableService {

    private final TimeTableRepository timeTableRepository;
    private final LectureRepository lectureRepository;

    public List<TimeTableResponse> getAll() {
        return timeTableRepository.findAll()
                .stream()
                .map(TimeTableMapper::mapToResponse)
                .toList();
    }

    public TimeTableResponse getById(Long id) {
        TimeTable timeTable = timeTableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("TimeTable not found"));

        return TimeTableMapper.mapToResponse(timeTable);
    }

    public TimeTableResponse create(TimeTableRequest request) {
        TimeTable response = new TimeTable();
        return saveTimeTable(request, response);
    }

    public TimeTableResponse update(Long id, TimeTableRequest request) {
        TimeTable response = timeTableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("TimeTable not found"));

        return saveTimeTable(request, response);
    }

    public void delete(Long id) {
        if (!timeTableRepository.existsById(id)) {
            throw new RuntimeException("TimeTable not found");
        }
        timeTableRepository.deleteById(id);
    }

    public void deleteAll() {
        timeTableRepository.deleteAll();
    }

    // --- Helper methods --- //

    private TimeTableResponse saveTimeTable(TimeTableRequest request, TimeTable response) {

        response.setLectures(
                request.getLectureIds()
                        .stream()
                        .map(lectureRepository::getReferenceById)
                        .toList()
        );
        response.setReport(null);
        response.setFitness(null);

        TimeTable updated = timeTableRepository.save(response);
        return TimeTableMapper.mapToResponse(updated);
    }
}
