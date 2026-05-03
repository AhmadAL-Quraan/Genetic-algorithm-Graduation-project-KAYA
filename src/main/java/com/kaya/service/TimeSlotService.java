package com.kaya.service;

import com.kaya.dto.request.TimeSlotRequest;
import com.kaya.dto.response.TimeSlotResponse;
import com.kaya.dto.mapper.TimeSlotMapper;
import com.kaya.model.TimeSlot;
import com.kaya.repository.TimeSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TimeSlotService {

    private final TimeSlotRepository timeSlotRepository;

    public List<TimeSlotResponse> getAll() {
        return timeSlotRepository.findAll()
                .stream()
                .map(TimeSlotMapper::mapToResponse)
                .toList();
    }

    public TimeSlotResponse getById(Long id) {
        TimeSlot timeSlot = timeSlotRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("TimeSlot not found"));

        return TimeSlotMapper.mapToResponse(timeSlot);
    }

    public TimeSlot getEntityById(Long id) {
        return timeSlotRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("TimeSlot not found"));
    }

    public TimeSlotResponse create(TimeSlotRequest request) {
        TimeSlot response = new TimeSlot();
        return saveTimeSlot(request, response);
    }

    public List<TimeSlotResponse> createBulk(List<TimeSlotRequest> request) {
        List<TimeSlotResponse> l = new ArrayList<>();

        for (TimeSlotRequest timeSlotRequest : request) {
            TimeSlot response = new TimeSlot();
            l.add(saveTimeSlot(timeSlotRequest, response));
        }
        return l;
    }

    public TimeSlotResponse update(Long id, TimeSlotRequest request) {
        TimeSlot response = timeSlotRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("TimeSlot not found"));

        return saveTimeSlot(request, response);
    }

    public void delete(Long id) {
        if (!timeSlotRepository.existsById(id)) {
            throw new RuntimeException("TimeSlot not found");
        }
        timeSlotRepository.deleteById(id);
    }

    public void deleteAll() {
        timeSlotRepository.deleteAll();
    }

    // --- Helper methods --- //

    private TimeSlotResponse saveTimeSlot(TimeSlotRequest request, TimeSlot response) {

        response.setStartTime(request.getStartTime());
        response.setEndTime(request.getEndTime());
        response.setDays(request.getDays());
        response.setTeachingMethod(request.getTeachingMethod());

        TimeSlot updated = timeSlotRepository.save(response);
        return TimeSlotMapper.mapToResponse(updated);
    }
}
