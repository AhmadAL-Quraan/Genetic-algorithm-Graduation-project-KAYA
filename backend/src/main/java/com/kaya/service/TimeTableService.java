package com.kaya.service;

import com.kaya.dto.mapper.FitnessReportMapper;
import com.kaya.dto.mapper.TimeTableMapper;
import com.kaya.dto.request.TimeTableRequest;
import com.kaya.dto.response.TimeTableResponse;
import com.kaya.model.Lecture;
import com.kaya.model.TimeTable;
import com.kaya.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TimeTableService {

    private final TimeTableRepository timeTableRepository;
    private final LectureRepository lectureRepository;
    private final FitnessReportService fitnessReportService;
    private final CourseService courseService;
    private final RoomService roomService;
    private final TimeSlotService timeSlotService;

    @Transactional(readOnly = true)
    public List<TimeTableResponse> getAll() {
        return timeTableRepository.findAll()
                .stream()
                .map(TimeTableMapper::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
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

    private TimeTableResponse saveTimeTable(TimeTableRequest request, TimeTable response) {
        if (request.getFitnessReport() != null) {
            var savedReport = fitnessReportService.create(request.getFitnessReport());
            response.setReport(FitnessReportMapper.mapToEntity(savedReport));
        }

        if (request.getLectures() != null) {
            List<Lecture> lectures = request.getLectures();
            for (Lecture lecture : lectures) {
                if (lecture.getCourse() != null && lecture.getCourse().getId() != null) {
                    lecture.setCourse(courseService.getEntityById(lecture.getCourse().getId()));
                }
                if (lecture.getRoom() != null && lecture.getRoom().getId() != null) {
                    lecture.setRoom(roomService.getEntityById(lecture.getRoom().getId()));
                }
                if (lecture.getTimeSlot() != null && lecture.getTimeSlot().getId() != null) {
                    lecture.setTimeSlot(timeSlotService.getEntityById(lecture.getTimeSlot().getId()));
                }
            }
            List<Lecture> saved = lectureRepository.saveAll(lectures);
            response.setLectures(saved);
        }

        TimeTable updated = timeTableRepository.save(response);
        return TimeTableMapper.mapToResponse(updated);
    }
}