package com.kaya.service;

import com.kaya.dataManager.manualEntry.ManualEntry;
import com.kaya.dataManager.manualEntry.ManualEntryRequest;
import com.kaya.dataManager.manualEntry.ManualEntryResponse;
import com.kaya.dto.request.TimeSlotRequest;
import com.kaya.dto.response.TimeSlotResponse;
import com.kaya.mapper.TimeSlotMapper;
import com.kaya.model.TimeSlot;
import com.kaya.repository.TimeSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TimeSlotService {

    private final TimeSlotRepository courseRepository;

    public List<TimeSlotResponse> getAll() {
        return courseRepository.findAll()
                .stream()
                .map(TimeSlotMapper::mapToResponse)
                .toList();
    }

    public TimeSlotResponse getById(Long id) {
        TimeSlot course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("TimeSlot not found"));

        return TimeSlotMapper.mapToResponse(course);
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
        TimeSlot response = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("TimeSlot not found"));

        return saveTimeSlot(request, response);
    }

    public void delete(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new RuntimeException("TimeSlot not found");
        }
        courseRepository.deleteById(id);
    }

    // --- Helper methods --- //

    private TimeSlotResponse saveTimeSlot(TimeSlotRequest request, TimeSlot response) {

        response.setStartTime(request.getStartTime());
        response.setEndTime(request.getEndTime());
        response.setDays(request.getDays());

        TimeSlot updated = courseRepository.save(response);
        return TimeSlotMapper.mapToResponse(updated);
    }
}
