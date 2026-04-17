package com.kaya.dataManager.manualEntryGenerator;

import com.kaya.algorithm.run.SchedulerApp;
import com.kaya.dataManager.manualEntry.*;
import com.kaya.dto.request.LectureRequest;
import com.kaya.dto.response.LectureResponse;
import com.kaya.mapper.LectureMapper;
import com.kaya.model.Lecture;
import com.kaya.repository.LectureRepository;
import com.kaya.repository.TimeSlotRepository;
import com.kaya.service.LectureService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class manualEntryGeneratorService {

    private final ManualEntryRepository manualEntryRepository;
    private final LectureRepository lectureRepository;
    private final LectureService lectureService;

    private final TimeSlotRepository timeSlotRepository;


    public ArrayList<Lecture> create() {
        lectureRepository.deleteAll();
        return prepareData(getAll());
    }

    private List<ManualEntryResponse> getAll() {
        return manualEntryRepository.findAll()
                .stream()
                .map(ManualEntryMapper::mapToDTO)
                .toList();
    }

    private ArrayList<Lecture> prepareData(List<ManualEntryResponse> retrievedData) {

        ArrayList<Lecture> preparedForUse = new ArrayList<>();

        for (ManualEntryResponse data : retrievedData) {

            LectureRequest request = new LectureRequest();
            request.setCourseId(data.getCourseId());
            request.setInstructor(data.getInstructor());
            request.setNumber(1L);
            request.setTimeSlotId((long) (Math.random() * 14) + 1);
            request.setRoomId((long) (Math.random() * 50) + 1);

            LectureResponse response = lectureService.create(request);

            preparedForUse.add(LectureMapper.mapToEntity(response));
        }

        SchedulerApp.go(preparedForUse);

        return preparedForUse;
    }
}
