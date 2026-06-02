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

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

    // Modified: The method now returns a List because a single window is split into multiple time slots
    @Transactional
    public List<TimeSlotResponse> create(TimeSlotRequest request) {
        List<TimeSlot> generatedSlots = saveTimeSlot(request);
        List<TimeSlot> savedSlots = timeSlotRepository.saveAll(generatedSlots);
        return savedSlots.stream().map(TimeSlotMapper::mapToResponse).toList();
    }

    @Transactional
    public List<TimeSlotResponse> createBulk(List<TimeSlotRequest> requests) {
        List<TimeSlot> allGenerated = new ArrayList<>();
        for (TimeSlotRequest request : requests) {
            allGenerated.addAll(saveTimeSlot(request));
        }
        List<TimeSlot> savedSlots = timeSlotRepository.saveAll(allGenerated);
        return savedSlots.stream().map(TimeSlotMapper::mapToResponse).toList();
    }

    public TimeSlotResponse update(Long id, TimeSlotRequest request) {
        TimeSlot response = timeSlotRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("TimeSlot not found"));

        response.setStartTime(request.getStartTime());
        response.setEndTime(request.getEndTime());
        response.setDays(request.getDays());
        response.setTeachingMethod(request.getTeachingMethod());
        response.setDurationMinutes(request.getDurationMinutes());

        TimeSlot updated = timeSlotRepository.save(response);
        return TimeSlotMapper.mapToResponse(updated);
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

    private List<TimeSlot> saveTimeSlot(TimeSlotRequest request) {
        if (request.getDurationMinutes() == null || request.getStartTime() == null || request.getEndTime() == null) {
            throw new IllegalArgumentException("Duration, Start Time, and End Time must be provided.");
        }

        long windowMinutes = java.time.Duration.between(request.getStartTime(), request.getEndTime()).toMinutes();
        if (windowMinutes < request.getDurationMinutes()) {
            throw new IllegalArgumentException(
                    "Window is only " + windowMinutes + " min wide but duration is " + request.getDurationMinutes() +
                            " min — the window must be at least as wide as the lecture duration.");
        }

        List<TimeSlot> expanded = new ArrayList<>();
        int duration = request.getDurationMinutes();
        LocalTime windowEnd = request.getEndTime();

        LocalTime cursor = request.getStartTime();

        while (!cursor.plusMinutes(duration).isAfter(windowEnd)) {
            LocalTime slotEnd = cursor.plusMinutes(duration);

            TimeSlot piece = new TimeSlot();
            piece.setStartTime(cursor);
            piece.setEndTime(slotEnd);

            piece.setDays(request.getDays());

            piece.setTeachingMethod(request.getTeachingMethod());
            piece.setDurationMinutes(request.getDurationMinutes());

            expanded.add(piece);
            cursor = slotEnd;
        }

        return expanded;
    }
}