package com.kaya.dataManager.manualEntryGenerator;

import com.kaya.algorithm.run.StartPoint;
import com.kaya.dataManager.manualEntry.*;
import com.kaya.dto.request.LectureRequest;
import com.kaya.dto.response.LectureResponse;
import com.kaya.dto.response.RoomResponse;
import com.kaya.dto.response.TimeSlotResponse;
import com.kaya.mapper.LectureMapper;
import com.kaya.mapper.RoomMapper;
import com.kaya.mapper.TimeSlotMapper;
import com.kaya.model.Lecture;
import com.kaya.model.Room;
import com.kaya.model.TimeSlot;
import com.kaya.repository.LectureRepository;
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
        lectureService.deleteAll();
        makeRequest(getLectures(), getRooms(), getTimeSlots());
    }

    private void makeRequest(List<Lecture> lectures, List<Room> rooms, List<TimeSlot> timeSlots) {
        StartPoint.runFromDatabase(lectures, rooms, timeSlots);
    }

    private List<Room> getRooms() {

        List<Room> rooms = new ArrayList<>();
        List<RoomResponse> roomResponses = roomService.getAll();
        for (RoomResponse response : roomResponses){
            rooms.add(RoomMapper.mapToEntity(response));
        }
        return rooms;
    }

    private List<TimeSlot> getTimeSlots() {

        List<TimeSlot> timeSlots = new ArrayList<>();
        List<TimeSlotResponse> timeSlotResponses = timeSlotService.getAll();
        for (TimeSlotResponse response : timeSlotResponses){
            timeSlots.add(TimeSlotMapper.mapToEntity(response));
        }
        return timeSlots;
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
