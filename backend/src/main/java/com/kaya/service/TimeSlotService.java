package com.kaya.service;

import com.kaya.dto.request.TimeSlotRequest;
import com.kaya.dto.response.TimeSlotResponse;
import com.kaya.dto.mapper.TimeSlotMapper;
import com.kaya.model.TimeSlot;
import com.kaya.repository.LectureRepository;
import com.kaya.repository.TimeSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TimeSlotService {

    private final TimeSlotRepository timeSlotRepository;
    private final LectureRepository lectureRepository;

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

    @Transactional
    public void delete(Long id) {
        if (!timeSlotRepository.existsById(id)) {
            throw new RuntimeException("TimeSlot not found");
        }
        lectureRepository.detachTimeSlot(id);
        timeSlotRepository.deleteById(id);
    }

    @Transactional
    public void deleteAll() {
        timeSlotRepository.findAll().forEach(slot ->
                lectureRepository.detachTimeSlot(slot.getId())
        );
        timeSlotRepository.deleteAll();
    }

    private TimeSlotResponse saveTimeSlot(TimeSlotRequest request, TimeSlot response) {
        // Validate: window must be wide enough to fit at least one slot
        if (request.getDurationMinutes() != null && request.getStartTime() != null && request.getEndTime() != null) {
            long windowMinutes = java.time.Duration.between(request.getStartTime(), request.getEndTime()).toMinutes();
            if (windowMinutes < request.getDurationMinutes()) {
                throw new IllegalArgumentException(
                    "Window is only " + windowMinutes + " min wide but duration is " + request.getDurationMinutes() +
                    " min — the window must be at least as wide as the lecture duration.");
            }
        }
        response.setStartTime(request.getStartTime());
        response.setEndTime(request.getEndTime());
        response.setDays(request.getDays());
        response.setTeachingMethod(request.getTeachingMethod());
        response.setDurationMinutes(request.getDurationMinutes());
        TimeSlot updated = timeSlotRepository.save(response);
        return TimeSlotMapper.mapToResponse(updated);
    }
}
