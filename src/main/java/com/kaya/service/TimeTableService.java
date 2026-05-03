package com.kaya.service;

import com.kaya.dto.request.TimeTableRequest;
import com.kaya.dto.response.FitnessReportResponse;
import com.kaya.dto.response.TimeTableResponse;
import com.kaya.dto.mapper.FitnessReportMapper;
import com.kaya.dto.mapper.TimeTableMapper;
import com.kaya.model.Lecture;
import com.kaya.model.TimeTable;
import com.kaya.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TimeTableService {

    // Direct access
    private final TimeTableRepository timeTableRepository;
    private final LectureRepository lectureRepository;

    // Access through services
    private final FitnessReportService fitnessReportService;
    private final CourseService courseService;
    private final RoomService roomService;
    private final TimeSlotService timeSlotService;

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

        // 1. Set the lectures from the request
        response.setLectures(request.getLectures());

        // 2. Handle Fitness Report
        if (request.getFitnessReport() != null) {
            FitnessReportResponse fitnessReportResponse = fitnessReportService.create(request.getFitnessReport());
            response.setReport(FitnessReportMapper.mapToEntity(fitnessReportResponse));
        }

        // 3. Prepare and save Lectures (This is the most important part)
        List<Lecture> lecturesToSave = request.getLectures();

        // Re-attach associated entities (Course, Room, TimeSlot) to avoid detached entity issues
        for (Lecture lecture : lecturesToSave) {
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

        // Save lectures first
        List<Lecture> savedLectures = lectureRepository.saveAll(lecturesToSave);
        response.setLectures(savedLectures);

        // 4. Finally save the TimeTable
        TimeTable updated = timeTableRepository.save(response);

        return TimeTableMapper.mapToResponse(updated);
    }
}
