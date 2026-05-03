package com.kaya.dataManager.manualEntryGenerator;

import com.kaya.algorithm.run.StartPoint;
import com.kaya.dataManager.manualEntry.*;
import com.kaya.dto.request.LectureRequest;
import com.kaya.dto.response.LectureResponse;
import com.kaya.dto.mapper.LectureMapper;
import com.kaya.dto.mapper.RoomMapper;
import com.kaya.dto.mapper.TimeSlotMapper;
import com.kaya.dto.mapper.TimeTableMapper;
import com.kaya.model.Lecture;
import com.kaya.model.Room;
import com.kaya.model.TimeSlot;
import com.kaya.model.TimeTable;
import com.kaya.service.LectureService;
import com.kaya.service.RoomService;
import com.kaya.service.TimeSlotService;
import com.kaya.service.TimeTableService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class manualEntryGeneratorService {

    private final ManualEntryRepository manualEntryRepository;
    private final LectureService lectureService;
    private final RoomService roomService;
    private final TimeSlotService timeSlotService;
    private final TimeTableService timeTableService;

    public void create() {
        makeRequest(getLectures(), getRooms(), getTimeSlots());
    }

    private void makeRequest(List<Lecture> lectures, List<Room> rooms, List<TimeSlot> timeSlots) {
        TimeTable table = StartPoint.runFromDatabase(lectures, rooms, timeSlots);
        timeTableService.create(TimeTableMapper.mapToRequest(table));
    }

    private List<Room> getRooms() {
        return roomService.getAll().stream()
                .map(RoomMapper::mapToEntity)
                .toList();
    }

    private List<TimeSlot> getTimeSlots() {
        return timeSlotService.getAll().stream()
                .map(TimeSlotMapper::mapToEntity)
                .toList();
    }

    private List<Lecture> getLectures() {

        List<ManualEntryResponse> retrievedData = manualEntryRepository.findAll()
                .stream()
                .map(ManualEntryMapper::mapToDTO)
                .toList();

        ArrayList<Lecture> preparedLectures = new ArrayList<>();

        for (ManualEntryResponse data : retrievedData) {

            LectureRequest request = new LectureRequest();
            request.setCourseId(data.getCourseId());
            request.setInstructor(data.getInstructor());
            request.setNumber(1L);
            request.setTimeSlotId(null);
            request.setRoomId(null);

            LectureResponse response = lectureService.create(request);

            preparedLectures.add(LectureMapper.mapToEntity(response));
        }

        return preparedLectures;
    }
}
