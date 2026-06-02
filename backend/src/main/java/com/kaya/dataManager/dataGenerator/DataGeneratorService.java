package com.kaya.dataManager.dataGenerator;

import com.kaya.algorithm.FitnessCalculator;
import com.kaya.algorithm.GAConfig;
import com.kaya.algorithm.ProgressSnapshot;
import com.kaya.algorithm.run.StartPoint;
import com.kaya.dto.mapper.TimeTableMapper;
import com.kaya.dto.response.ProgressResponse;
import com.kaya.dto.response.TimeTableResponse;
import com.kaya.model.Lecture;
import com.kaya.model.Room;
import com.kaya.model.TimeSlot;
import com.kaya.model.TimeTable;
import com.kaya.repository.RoomRepository;
import com.kaya.repository.TimeSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DataGeneratorService {

    private final GAConfigMapper gaConfigMapper;
    private final LectureLoader lectureLoader;
    private final TimeTablePersister timeTablePersister;
    private final RoomRepository roomRepository;
    private final TimeSlotRepository timeSlotRepository;

    private volatile boolean cancelRequested = false;
    private volatile ProgressResponse currentProgress = null;

    public void cancelGeneration() {
        cancelRequested = true;
    }

    public ProgressResponse getProgress() {
        return currentProgress;
    }

    @Transactional
    public TimeTableResponse generate(Map<String, Object> configMap) {

        // Set data
        cancelRequested = false;
        GAConfig config = gaConfigMapper.map(configMap, new GAConfig());
        boolean useIslandModel = gaConfigMapper.resolveUseIslandModel(configMap);
        currentProgress = new ProgressResponse("initializing", 0, config.maxGenerations, 0, 0, 0, 0, 0.0);

        List<Lecture> lectures = lectureLoader.load();
        List<Room> rooms = roomRepository.findAll();
        List<TimeSlot> timeSlots = timeSlotRepository.findAll();

        validateTeachingMethods(lectures, timeSlots);

        final int maxGen = config.maxGenerations;

        // Run the algorithm
        TimeTable best = StartPoint.runAlgorithm(lectures, rooms, timeSlots, config, useIslandModel,
                () -> cancelRequested,
                (ProgressSnapshot snap) -> {
                    String phase = snap.perfect ? "perfect" : "evolving";
                    currentProgress = new ProgressResponse(
                            phase,
                            snap.generation,
                            maxGen,
                            snap.bestFitness,
                            snap.roomConflicts,
                            snap.instructorConflicts,
                            snap.studentConflicts,
                            snap.mutationRate
                    );
                });

        currentProgress = new ProgressResponse("saving", 0, maxGen, 0, 0, 0, 0, 0.0);
        //FitnessCalculator.calculateFitness(best);
        SectionGenerator.generate(best);
        TimeTableResponse result = TimeTableMapper.mapToResponse(timeTablePersister.persist(best));

        currentProgress = null;
        return result;
    }

    private void validateTeachingMethods(List<Lecture> lectures, List<TimeSlot> timeSlots) {
        Set<String> neededMethods = lectures.stream()
                .filter(l -> l.getCourse() != null && l.getCourse().getTeachingMethod() != null)
                .map(l -> l.getCourse().getTeachingMethod().name())
                .collect(Collectors.toSet());

        Set<String> availableMethods = timeSlots.stream()
                .filter(ts -> ts.getTeachingMethod() != null)
                .map(ts -> ts.getTeachingMethod().name())
                .collect(Collectors.toSet());

        Set<String> missing = neededMethods.stream()
                .filter(m -> !availableMethods.contains(m))
                .collect(Collectors.toSet());

        if (!missing.isEmpty()) {
            currentProgress = null;
            throw new IllegalStateException(
                    "No schedulable time slots for teaching method(s): " + missing +
                            ". Check that your time window is at least as wide as the lecture duration.");
        }
    }
}