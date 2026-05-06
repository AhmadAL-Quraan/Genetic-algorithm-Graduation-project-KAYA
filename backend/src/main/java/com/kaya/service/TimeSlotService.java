package com.kaya.service;

import com.kaya.dto.request.TimeSlotRequest;
import com.kaya.dto.response.TimeSlotResponse;
import com.kaya.mapper.TimeSlotMapper;
import com.kaya.model.Lecture;
import com.kaya.model.TimeSlot;
import com.kaya.repository.LectureRepository;
import com.kaya.repository.TimeSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TimeSlotService {

    private final TimeSlotRepository courseRepository;
    private final LectureRepository lectureRepository;

    public List<TimeSlotResponse> getAll() {
        return courseRepository.findAll().stream().map(TimeSlotMapper::mapToResponse).toList();
    }

    public TimeSlotResponse getById(Long id) {
        TimeSlot ts = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("TimeSlot not found"));
        return TimeSlotMapper.mapToResponse(ts);
    }

    public TimeSlotResponse create(TimeSlotRequest request) {
        validateNoOverlap(request, null);
        return saveTimeSlot(request, new TimeSlot());
    }

    public TimeSlotResponse update(Long id, TimeSlotRequest request) {
        TimeSlot ts = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("TimeSlot not found"));
        validateNoOverlap(request, id);
        return saveTimeSlot(request, ts);
    }

    @Transactional
    public void delete(Long id) {
        if (!courseRepository.existsById(id)) throw new RuntimeException("TimeSlot not found");
        List<Lecture> lectures = lectureRepository.findAll().stream()
                .filter(l -> l.getTimeSlot() != null && id.equals(l.getTimeSlot().getId()))
                .toList();
        lectures.forEach(l -> l.setTimeSlot(null));
        lectureRepository.saveAll(lectures);
        courseRepository.deleteById(id);
    }

    @Transactional
    public void deleteAll() {
        lectureRepository.findAll().forEach(l -> l.setTimeSlot(null));
        lectureRepository.saveAll(lectureRepository.findAll());
        courseRepository.deleteAll();
    }

    private TimeSlotResponse saveTimeSlot(TimeSlotRequest request, TimeSlot ts) {
        ts.setStartTime(request.getStartTime());
        ts.setEndTime(request.getEndTime());
        ts.setDays(request.getDays());
        ts.setTeachingMethod(request.getTeachingMethod());
        ts.setDurationMinutes(request.getDurationMinutes());
        return TimeSlotMapper.mapToResponse(courseRepository.save(ts));
    }

    /**
     * Rejects the request if any existing time slot for the same teaching method
     * shares at least one day AND has an overlapping time window.
     * Pass excludeId = the slot's own id when updating (so it doesn't conflict with itself).
     */
    private void validateNoOverlap(TimeSlotRequest request, Long excludeId) {
        List<TimeSlot> sameMethod = courseRepository.findAll().stream()
                .filter(ts -> ts.getTeachingMethod() == request.getTeachingMethod())
                .filter(ts -> excludeId == null || !ts.getId().equals(excludeId))
                .toList();

        for (TimeSlot existing : sameMethod) {
            if (existing.getDays() == null || existing.getDays().isEmpty()) continue;

            boolean sharedDay = !Collections.disjoint(existing.getDays(), request.getDays());
            if (!sharedDay) continue;

            // Two windows overlap when: newStart < existingEnd AND existingStart < newEnd
            boolean timeOverlap = request.getStartTime().isBefore(existing.getEndTime())
                    && existing.getStartTime().isBefore(request.getEndTime());

            if (timeOverlap) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Overlapping window: a " + request.getTeachingMethod()
                        + " slot already exists from " + existing.getStartTime().toString().substring(0, 5)
                        + " to " + existing.getEndTime().toString().substring(0, 5)
                        + " on overlapping days. Please adjust the time or days.");
            }
        }
    }
}
