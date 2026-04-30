package com.kaya.service;

import com.kaya.dto.request.TimeSlotRequest;
import com.kaya.dto.response.TimeSlotResponse;
import com.kaya.mapper.TimeSlotMapper;
import com.kaya.model.Lecture;
import com.kaya.model.TimeSlot;
import com.kaya.repository.LectureRepository;
import com.kaya.repository.TimeSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
        return saveTimeSlot(request, new TimeSlot());
    }

    public TimeSlotResponse update(Long id, TimeSlotRequest request) {
        TimeSlot ts = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("TimeSlot not found"));
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
        return TimeSlotMapper.mapToResponse(courseRepository.save(ts));
    }
}
