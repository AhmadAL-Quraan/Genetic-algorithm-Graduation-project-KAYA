package com.kaya.service;

import com.kaya.algorithm.GAConfig;
import com.kaya.algorithm.ProgressSnapshot;
import com.kaya.algorithm.run.StartPoint;
import com.kaya.dto.mapper.FitnessReportMapper;
import com.kaya.dto.mapper.TimeTableMapper;
import com.kaya.dto.request.TimeTableRequest;
import com.kaya.dto.response.ProgressResponse;
import com.kaya.dto.response.TimeTableResponse;
import com.kaya.model.FitnessReport;
import com.kaya.model.Lecture;
import com.kaya.model.Room;
import com.kaya.model.TimeSlot;
import com.kaya.model.TimeTable;
import com.kaya.dataManager.manualEntry.ManualEntry;
import com.kaya.dataManager.manualEntry.ManualEntryRepository;
import com.kaya.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.kaya.dataManager.SectionGenerator;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TimeTableService {

    private final TimeTableRepository timeTableRepository;
    private final LectureRepository lectureRepository;
    private final RoomRepository roomRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final FitnessReportRepository fitnessReportRepository;
    private final FitnessReportService fitnessReportService;
    private final CourseService courseService;
    private final RoomService roomService;
    private final TimeSlotService timeSlotService;
    private final ManualEntryRepository manualEntryRepository;
    private final InstructorRepository instructorRepository;

    private volatile boolean cancelRequested = false;
    private volatile ProgressResponse currentProgress = null;

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

    public void cancelGeneration() {
        cancelRequested = true;
    }

    public ProgressResponse getProgress() {
        return currentProgress;
    }

    @Transactional
    public TimeTableResponse generate(Map<String, Object> configMap) {
        cancelRequested = false;

        GAConfig config = new GAConfig();
        if (configMap != null) {
            if (configMap.containsKey("maxGenerations"))
                config.maxGenerations = toInt(configMap.get("maxGenerations"), config.maxGenerations);
            if (configMap.containsKey("populationSize"))
                config.populationSize = toInt(configMap.get("populationSize"), config.populationSize);
            if (configMap.containsKey("elitismCount"))
                config.elitismCount = toInt(configMap.get("elitismCount"), config.elitismCount);
            if (configMap.containsKey("tournamentSize"))
                config.tournamentSize = toInt(configMap.get("tournamentSize"), config.tournamentSize);
            if (configMap.containsKey("initialMutationRate"))
                config.initialMutationRate = toDouble(configMap.get("initialMutationRate"), config.initialMutationRate);
            if (configMap.containsKey("mutationImpactRatio"))
                config.mutationImpactRatio = toDouble(configMap.get("mutationImpactRatio"), config.mutationImpactRatio);
        }

        currentProgress = new ProgressResponse("initializing", 0, config.maxGenerations, 0, 0, 0, 0, 0.0);

        List<Lecture> lectures = lectureRepository.findByTimetableIdIsNull()
                .stream()
                .filter(l -> l.getCourse() != null)
                .toList();

        // Fallback: if no template lectures exist, build them in-memory from ManualEntry rows
        if (lectures.isEmpty()) {
            List<ManualEntry> manualEntries = manualEntryRepository.findAll();
            if (!manualEntries.isEmpty()) {
                List<Lecture> fromManual = new ArrayList<>();
                for (ManualEntry entry : manualEntries) {
                    if (entry.getCourseId() == null) continue;
                    try {
                        Lecture l = new Lecture();
                        l.setId(null);
                        l.setCourse(courseService.getEntityById(entry.getCourseId()));
                        l.setInstructor(entry.getInstructor());
                        fromManual.add(l);
                    } catch (RuntimeException ignored) {
                        // course may have been deleted; skip this entry
                    }
                }
                lectures = fromManual;
            }
        }

        // Guard: must have at least one lecture section to schedule
        if (lectures.isEmpty()) {
            currentProgress = null;
            throw new IllegalStateException(
                "No lecture sections found. Add lecture sections on the Lectures page (or manual entries) before generating.");
        }

        List<Room> rooms = roomRepository.findAll();
        List<TimeSlot> timeSlots = expandAndPersistWindows(timeSlotRepository.findAll());

        // Guard: every teaching method required by at least one lecture must have ≥1 slot
        Set<String> neededMethods = lectures.stream()
                .filter(l -> l.getCourse() != null && l.getCourse().getTeachingMethod() != null)
                .map(l -> l.getCourse().getTeachingMethod().name())
                .collect(Collectors.toSet());
        Set<String> availableMethods = timeSlots.stream()
                .filter(ts -> ts.getTeachingMethod() != null)
                .map(ts -> ts.getTeachingMethod().name())
                .collect(Collectors.toSet());
        Set<String> missing = new java.util.HashSet<>(neededMethods);
        missing.removeAll(availableMethods);
        if (!missing.isEmpty()) {
            currentProgress = null;
            throw new IllegalStateException(
                "No schedulable time slots for teaching method(s): " + missing +
                ". Check that your time window is at least as wide as the lecture duration.");
        }

        final int maxGen = config.maxGenerations;

        TimeTable best = StartPoint.runAlgorithm(lectures, rooms, timeSlots, config,false,
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

        SectionGenerator.generate(best);
        TimeTableResponse result = TimeTableMapper.mapToResponse(persistGeneratedTimeTable(best));

        currentProgress = null;
        return result;
    }

    @Transactional
    public TimeTable persistGeneratedTimeTable(TimeTable best) {
        // Build a fresh FitnessReport (no conflicting lecture references to avoid join-table conflicts)
        FitnessReport freshReport = new FitnessReport();
        freshReport.setRoomConflicts(best.getReport().getRoomConflicts());
        freshReport.setInstructorConflicts(best.getReport().getInstructorConflicts());
        freshReport.setStudentConflicts(best.getReport().getStudentConflicts());
        freshReport.setTotalPenalty(best.getReport().getTotalPenalty());
        freshReport.setConflictingLectures(new HashSet<>());

        // Create brand-new Lecture rows for this timetable (null id = INSERT not UPDATE)
        List<Lecture> freshLectures = new ArrayList<>();
        for (Lecture l : best.getLectures()) {
            Lecture fresh = new Lecture();
            fresh.setId(null);
            if (l.getCourse() != null && l.getCourse().getId() != null) {
                fresh.setCourse(courseService.getEntityById(l.getCourse().getId()));
            }
            if (l.getRoom() != null && l.getRoom().getId() != null) {
                fresh.setRoom(roomService.getEntityById(l.getRoom().getId()));
            }
            if (l.getTimeSlot() != null && l.getTimeSlot().getId() != null) {
                fresh.setTimeSlot(timeSlotService.getEntityById(l.getTimeSlot().getId()));
            }
            if (l.getInstructor() != null && l.getInstructor().getId() != null) {
                fresh.setInstructor(
                        instructorRepository.findById(l.getInstructor().getId()).orElse(null)
                );
            }
            freshLectures.add(fresh);
        }

        // Auto-assign section numbers per course (1, 2, 3… for each section of the same course)
        Map<Long, Integer> sectionCounters = new HashMap<>();
        for (Lecture fresh : freshLectures) {
            if (fresh.getCourse() != null) {
                Long courseId = fresh.getCourse().getId();
                int section = sectionCounters.merge(courseId, 1, Integer::sum);
                fresh.setSectionNumber(section);
            }
        }

        List<Lecture> savedLectures = lectureRepository.saveAll(freshLectures);
        FitnessReport savedReport = fitnessReportRepository.save(freshReport);

        TimeTable tt = new TimeTable();
        tt.setLectures(savedLectures);
        tt.setReport(savedReport);
        return timeTableRepository.save(tt);
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

    /**
     * Expands "window" timeslots (those with durationMinutes set) into individual
     * fixed-length slots. Slots are persisted to DB (find-or-create) so they get
     * real IDs the algorithm can reference. Window slots themselves are passed
     * through unchanged — the algorithm will ignore them because StartPoint only
     * uses slots that match the course's teaching method, and windows are the parent.
     */
    @Transactional
    public List<TimeSlot> expandAndPersistWindows(List<TimeSlot> allSlots) {
        // Separate windows (have durationMinutes) from already-individual slots
        List<TimeSlot> windows     = allSlots.stream().filter(s -> s.getDurationMinutes() != null).collect(Collectors.toList());
        List<TimeSlot> individuals = allSlots.stream().filter(s -> s.getDurationMinutes() == null).collect(Collectors.toList());

        if (windows.isEmpty()) {
            return allSlots; // nothing to expand
        }

        // Build a lookup key set of already-existing individual slots
        Map<String, TimeSlot> existingMap = new HashMap<>();
        for (TimeSlot s : individuals) {
            existingMap.put(slotKey(s), s);
        }

        // Only return slots derived from the CURRENT windows — not orphaned individuals
        // from previously deleted windows. existingMap is only used for find-or-create.
        List<TimeSlot> result = new ArrayList<>();

        for (TimeSlot window : windows) {
            int duration = window.getDurationMinutes();
            LocalTime windowEnd = window.getEndTime();

            // Expand per day so the algorithm treats MON 08:00 and WED 08:00 as
            // separate slots. This doubles the effective pool and allows the genetic
            // algorithm to avoid student-year conflicts entirely.
            for (DayOfWeek day : window.getDays()) {
                LocalTime cursor = window.getStartTime();
                while (!cursor.plusMinutes(duration).isAfter(windowEnd)) {
                    LocalTime slotEnd = cursor.plusMinutes(duration);

                    TimeSlot candidate = new TimeSlot();
                    candidate.setStartTime(cursor);
                    candidate.setEndTime(slotEnd);
                    candidate.setDays(new HashSet<>(Set.of(day)));
                    candidate.setTeachingMethod(window.getTeachingMethod());
                    candidate.setDurationMinutes(null);

                    String key = slotKey(candidate);
                    TimeSlot saved = existingMap.computeIfAbsent(key, k -> timeSlotRepository.save(candidate));
                    result.add(saved);

                    cursor = slotEnd;
                }
            }
        }

        System.out.println("[TimeTableService] Expanded " + windows.size() + " window(s) into "
                + (result.size() - individuals.size()) + " individual slot(s) for the algorithm.");
        return result;
    }

    private String slotKey(TimeSlot s) {
        List<String> sortedDays = s.getDays().stream().map(Enum::name).sorted().collect(Collectors.toList());
        return s.getStartTime() + "|" + s.getEndTime() + "|" + s.getTeachingMethod() + "|" + sortedDays;
    }

    private int toInt(Object val, int def) {
        if (val instanceof Number) return ((Number) val).intValue();
        try { return Integer.parseInt(val.toString()); } catch (Exception e) { return def; }
    }

    private double toDouble(Object val, double def) {
        if (val instanceof Number) return ((Number) val).doubleValue();
        try { return Double.parseDouble(val.toString()); } catch (Exception e) { return def; }
    }
}
