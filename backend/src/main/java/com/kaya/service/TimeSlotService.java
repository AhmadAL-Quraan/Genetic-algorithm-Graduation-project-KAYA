package com.kaya.service;

import com.kaya.dto.request.TimeSlotRequest;
import com.kaya.dto.response.TimeSlotResponse;
import com.kaya.dto.mapper.TimeSlotMapper;
import com.kaya.model.TimeSlot;
import com.kaya.repository.TimeSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

    public List<TimeSlotResponse> create(TimeSlotRequest request) {
        return saveTimeSlot(request);
    }

//    public List<TimeSlotResponse> createBulk(List<TimeSlotRequest> request) {
//        List<TimeSlotResponse> l = new ArrayList<>();
//
//        for (TimeSlotRequest timeSlotRequest : request) {
//            TimeSlot response = new TimeSlot();
//            l.add(saveTimeSlot(timeSlotRequest));
//        }
//        return l;
//    }

//    public List<TimeSlotResponse> update(Long id, TimeSlotRequest request) {
//        TimeSlot response = timeSlotRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("TimeSlot not found"));
//
//        return saveTimeSlot(request);
//    }

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

//    private TimeSlotResponse saveTimeSlot(TimeSlotRequest request, TimeSlot response) {
//
//        response.setStartTime(request.getStartTime());
//        response.setEndTime(request.getEndTime());
//        response.setDays(request.getDays());
//        response.setTeachingMethod(request.getTeachingMethod());
//
//        TimeSlot updated = timeSlotRepository.save(response);
//        return TimeSlotMapper.mapToResponse(updated);
//    }

    private List<TimeSlotResponse> saveTimeSlot(TimeSlotRequest request) {

        List<TimeSlotResponse> responses = new ArrayList<>();

        LocalTime currentStart = request.getStartTime();
        LocalTime windowEnd = request.getEndTime();
        int duration = request.getDurationMinutes();

        while (!currentStart.plusMinutes(duration).isAfter(windowEnd)) {

            LocalTime currentEnd = currentStart.plusMinutes(duration);

            TimeSlot timeSlot = new TimeSlot();

            timeSlot.setStartTime(currentStart);
            timeSlot.setEndTime(currentEnd);
            timeSlot.setDays(request.getDays());
            timeSlot.setTeachingMethod(request.getTeachingMethod());
            timeSlot.setDurationMinutes(duration);

            TimeSlot saved = timeSlotRepository.save(timeSlot);

            responses.add(TimeSlotMapper.mapToResponse(saved));

            currentStart = currentEnd;
        }

        return responses;
    }
}
