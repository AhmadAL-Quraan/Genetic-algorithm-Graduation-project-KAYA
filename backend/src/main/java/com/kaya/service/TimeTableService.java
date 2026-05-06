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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Consumer;
@Service
@RequiredArgsConstructor
public class TimeTableService {
    private final TimeTableRepository timeTableRepository;
    private final LectureRepository lectureRepository;
    private final RoomRepository roomRepository;
    private final TimeSlotRepository timeSlotRepository;
    private static volatile GenerationProgress latestProgress = null;
    private static volatile boolean cancelRequested = false;
    public static GenerationProgress getLatestProgress() { return latestProgress; }
    public static void requestCancel() { cancelRequested = true; }
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
    @Transactional
    public void delete(Long id) {
        TimeTable tt = timeTableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("TimeTable not found"));
        tt.getLectures().clear();
        timeTableRepository.delete(tt);
    }
    private TimeTableResponse saveTimeTable(TimeTableRequest request, TimeTable tt) {
        tt.setLectures(request.getLectureIds().stream()
                .map(lectureRepository::getReferenceById).toList());
        tt.setFitness(0L);
        tt.setGeneratedAt(LocalDateTime.now());
        return TimeTableMapper.mapToResponse(timeTableRepository.save(tt));
    }
    @Transactional
    public TimeTableResponse generate(GAConfig overrides) {
        return generateWithProgress(overrides, null);
    }
    @Transactional
    public TimeTableResponse generateWithProgress(GAConfig overrides, Consumer<GenerationProgress> progressCallback) {
        // Reset cancel flag at the start of every new run
        cancelRequested = false;
        List<Lecture> dbLectures = lectureRepository.findAllTemplateLectures();
        if (dbLectures.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "No lectures found. Please add courses and lectures first.");
        }
        // ── Build room pools ──────────────────────────────────────────────────
        Map<RoomType, HashSet<Room>> roomPools = new HashMap<>();
        for (RoomType rt : RoomType.values()) roomPools.put(rt, new HashSet<>());
        for (Room r : roomRepository.findAll()) {
            if (r.getRoomType() != null) {
                roomPools.computeIfAbsent(r.getRoomType(), k -> new HashSet<>()).add(r);
            }
        }
        // ── Build time pools from window slots ────────────────────────────────
        // Each TimeSlot in the DB is a window (startTime→endTime, days, teachingMethod, durationMinutes).
        // We expand every window into non-overlapping sub-slots of durationMinutes each.
        // IMPORTANT: each sub-slot gets its OWN copy of the days Set to avoid Hibernate
        // "shared references to a collection" errors when persisting.
        Map<TeachingMethod, HashSet<TimeSlot>> timePools = new HashMap<>();
        for (TeachingMethod tm : TeachingMethod.values()) timePools.put(tm, new HashSet<>());
        for (TimeSlot window : timeSlotRepository.findAll()) {
            if (window.getTeachingMethod() == null) continue;
            int dur = window.getDurationMinutes() != null ? window.getDurationMinutes() : 90;
            LocalTime t = window.getStartTime();
            while (!t.plusMinutes(dur).isAfter(window.getEndTime())) {
                LocalTime end = t.plusMinutes(dur);
                // Copy the days Set so no two TimeSlot instances share the same collection object
                TimeSlot sub = new TimeSlot(t, end, new HashSet<>(window.getDays()), window.getTeachingMethod());
                timePools.get(window.getTeachingMethod()).add(sub);
                t = t.plusMinutes(dur);
            }
        }
        // ── Validate each lecture has a viable pool ───────────────────────────
        for (Lecture l : dbLectures) {
            RoomType need = l.getCourse().getRequiredRoomType();
            if (need == null || roomPools.get(need) == null || roomPools.get(need).isEmpty()) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "No rooms of type '" + need + "' found. Please add at least one "
                        + need + " room before generating. (needed by "
                        + l.getCourse().getCourseSymbol() + " " + l.getCourse().getCourseNumber() + ")");
            }
            TeachingMethod method = l.getCourse().getTeachingMethod();
            if (method == null || timePools.get(method) == null || timePools.get(method).isEmpty()) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "No time window defined for '" + method + "'. Please add a time window for this teaching method first. (needed by "
                        + l.getCourse().getCourseSymbol() + " " + l.getCourse().getCourseNumber() + ")");
            }
        }
        // ── Build gene templates (one Lecture gene per DB lecture) ────────────
        ArrayList<Lecture> genes = new ArrayList<>();
        for (Lecture l : dbLectures) {
            Lecture g = new Lecture();
            g.setCourse(l.getCourse());
            g.setSectionNumber(l.getSectionNumber());
            g.setInstructor(l.getInstructor());
            genes.add(g);
        }
        Consumer<GenerationProgress> storeAndForward = p -> {
            latestProgress = p;
            if (progressCallback != null) progressCallback.accept(p);
        };
        storeAndForward.accept(GenerationProgress.initializing());
        GAConfig config = (overrides == null) ? new GAConfig() : overrides;
        EvolutionEngine engine = new EvolutionEngine(config);
        ArrayList<TimeTable> initial = engine.initializePopulation(genes, timePools, roomPools);
        // Pass cancel check into the evolution loop; if cancelled, engine throws → transaction rolls back
        ArrayList<TimeTable> finalPop = engine.evolveGenerations(
                initial, timePools, roomPools, storeAndForward, () -> cancelRequested);
        storeAndForward.accept(GenerationProgress.saving());
        TimeTable best = finalPop.get(0);
        FitnessCalculator.calculateFitness(best);
        best.setGeneratedAt(LocalDateTime.now());
        // Materialize virtual sub-slots (no DB id).
        // Use find-or-create: reuse an existing row if fields match (via equals),
        // otherwise persist a new one. An identity map avoids double-saving the same object.
        List<TimeSlot> existingSlots = timeSlotRepository.findAll();
        IdentityHashMap<TimeSlot, TimeSlot> materializedMap = new IdentityHashMap<>();
        for (Lecture l : best.getLectures()) {
            TimeSlot ts = l.getTimeSlot();
            if (ts != null && ts.getId() == null) {
                TimeSlot persisted = materializedMap.computeIfAbsent(ts, k -> {
                    return existingSlots.stream()
                            .filter(e -> e.equals(k))
                            .findFirst()
                            .orElseGet(() -> {
                                TimeSlot saved = timeSlotRepository.save(k);
                                existingSlots.add(saved);
                                return saved;
                            });
                });
                l.setTimeSlot(persisted);
            }
        }
        TimeTable saved = timeTableRepository.save(best);
        saved.setReport(best.getReport());
        return TimeTableMapper.mapToResponse(saved);
    }
}
