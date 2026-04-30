package com.kaya.service;

import com.kaya.algorithm.EvolutionEngine;
import com.kaya.algorithm.FitnessCalculator;
import com.kaya.algorithm.GAConfig;
import com.kaya.dto.request.TimeTableRequest;
import com.kaya.dto.response.GenerationProgress;
import com.kaya.dto.response.TimeTableResponse;
import com.kaya.mapper.TimeTableMapper;
import com.kaya.model.*;
import com.kaya.model.enums.RoomType;
import com.kaya.model.enums.TeachingMethod;
import com.kaya.repository.LectureRepository;
import com.kaya.repository.RoomRepository;
import com.kaya.repository.TimeSlotRepository;
import com.kaya.repository.TimeTableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class TimeTableService {

    private final TimeTableRepository timeTableRepository;
    private final LectureRepository lectureRepository;
    private final RoomRepository roomRepository;
    private final TimeSlotRepository timeSlotRepository;

    public List<TimeTableResponse> getAll() {
        return timeTableRepository.findAll().stream()
                .map(TimeTableMapper::mapToResponse).toList();
    }

    public TimeTableResponse getById(Long id) {
        TimeTable timeTable = timeTableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("TimeTable not found"));
        return TimeTableMapper.mapToResponse(timeTable);
    }

    public TimeTableResponse create(TimeTableRequest request) {
        return saveTimeTable(request, new TimeTable());
    }

    public TimeTableResponse update(Long id, TimeTableRequest request) {
        TimeTable response = timeTableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("TimeTable not found"));
        return saveTimeTable(request, response);
    }

    public void delete(Long id) {
        if (!timeTableRepository.existsById(id)) throw new RuntimeException("TimeTable not found");
        timeTableRepository.deleteById(id);
    }

    private TimeTableResponse saveTimeTable(TimeTableRequest request, TimeTable tt) {
        tt.setLectures(request.getLectureIds().stream()
                .map(lectureRepository::getReferenceById).toList());
        tt.setFitness(0L);
        tt.setGeneratedAt(LocalDateTime.now());
        return TimeTableMapper.mapToResponse(timeTableRepository.save(tt));
    }

    /**
     * Run the genetic algorithm on the existing lectures, rooms, and time slots,
     * and persist the best resulting timetable. The original lectures are NOT
     * modified — a new TimeTable with new Lecture rows is created (cascade=ALL).
     */
    @Transactional
    public TimeTableResponse generate(GAConfig overrides) {
        return generateWithProgress(overrides, null);
    }

    /**
     * Same as generate() but emits GenerationProgress events via the callback after
     * each generation. Safe to call from an SSE handler — the DB save happens at the end.
     */
    @Transactional
    public TimeTableResponse generateWithProgress(GAConfig overrides, Consumer<GenerationProgress> progressCallback) {
        List<Lecture> dbLectures = lectureRepository.findAll();
        if (dbLectures.isEmpty()) {
            throw new RuntimeException("Cannot generate: no lectures in database. Add lectures first.");
        }

        Map<RoomType, HashSet<Room>> roomPools = new HashMap<>();
        for (RoomType rt : RoomType.values()) roomPools.put(rt, new HashSet<>());
        for (Room r : roomRepository.findAll()) {
            if (r.getRoomType() != null) {
                roomPools.computeIfAbsent(r.getRoomType(), k -> new HashSet<>()).add(r);
            }
        }

        Map<TeachingMethod, HashSet<TimeSlot>> timePools = new HashMap<>();
        for (TeachingMethod tm : TeachingMethod.values()) timePools.put(tm, new HashSet<>());
        for (TimeSlot ts : timeSlotRepository.findAll()) {
            if (ts.getTeachingMethod() != null) {
                timePools.computeIfAbsent(ts.getTeachingMethod(), k -> new HashSet<>()).add(ts);
            }
        }

        for (Lecture l : dbLectures) {
            RoomType need = l.getCourse().getRequiredRoomType();
            TeachingMethod method = l.getCourse().getTeachingMethod();
            if (need == null || roomPools.get(need) == null || roomPools.get(need).isEmpty()) {
                throw new RuntimeException("No rooms of type " + need + " for course "
                        + l.getCourse().getCourseSymbol() + " " + l.getCourse().getCourseNumber());
            }
            if (method == null || timePools.get(method) == null || timePools.get(method).isEmpty()) {
                throw new RuntimeException("No time slots for teaching method " + method
                        + " (course " + l.getCourse().getCourseSymbol() + " "
                        + l.getCourse().getCourseNumber() + ")");
            }
        }

        ArrayList<Lecture> genes = new ArrayList<>();
        for (Lecture l : dbLectures) {
            Lecture g = new Lecture();
            g.setCourse(l.getCourse());
            g.setSectionNumber(l.getSectionNumber());
            g.setInstructor(l.getInstructor());
            genes.add(g);
        }

        if (progressCallback != null) {
            progressCallback.accept(GenerationProgress.initializing());
        }

        GAConfig config = (overrides == null) ? new GAConfig() : overrides;
        EvolutionEngine engine = new EvolutionEngine(config);

        ArrayList<TimeTable> initial = engine.initializePopulation(genes, timePools, roomPools);
        ArrayList<TimeTable> finalPop = engine.evolveGenerations(initial, timePools, roomPools, progressCallback);

        if (progressCallback != null) {
            progressCallback.accept(GenerationProgress.saving());
        }

        TimeTable best = finalPop.get(0);
        FitnessCalculator.calculateFitness(best);
        best.setGeneratedAt(LocalDateTime.now());

        TimeTable saved = timeTableRepository.save(best);
        saved.setReport(best.getReport());
        return TimeTableMapper.mapToResponse(saved);
    }
}
