package com.kaya.dataManager.dataGenerator;

import com.kaya.model.*;
import com.kaya.repository.FitnessReportRepository;
import com.kaya.repository.InstructorRepository;
import com.kaya.repository.LectureRepository;
import com.kaya.repository.TimeTableRepository;
import com.kaya.service.CourseService;
import com.kaya.service.RoomService;
import com.kaya.service.TimeSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Component
@RequiredArgsConstructor
public class TimeTablePersister {

    private final LectureRepository lectureRepository;
    private final FitnessReportRepository fitnessReportRepository;
    private final TimeTableRepository timeTableRepository;
    private final InstructorRepository instructorRepository;
    private final CourseService courseService;
    private final RoomService roomService;
    private final TimeSlotService timeSlotService;

    @Transactional
    public TimeTable persist(TimeTable best) {
        FitnessReport savedReport = buildAndSaveReport(best.getReport());
        List<Lecture> savedLectures = buildAndSaveLectures(best.getLectures());

        TimeTable tt = new TimeTable();
        tt.setLectures(savedLectures);
        tt.setReport(savedReport);
        return timeTableRepository.save(tt);
    }

    private FitnessReport buildAndSaveReport(FitnessReport source) {
        // Build a fresh FitnessReport (no conflicting lecture references to avoid join-table conflicts)
        FitnessReport freshReport = new FitnessReport();
        freshReport.setRoomConflicts(source.getRoomConflicts());
        freshReport.setInstructorConflicts(source.getInstructorConflicts());
        freshReport.setStudentConflicts(source.getStudentConflicts());
        freshReport.setTotalPenalty(source.getTotalPenalty());
        freshReport.setConflictingLectures(new HashSet<>());
        return fitnessReportRepository.save(freshReport);
    }

    private List<Lecture> buildAndSaveLectures(List<Lecture> sourceLectures) {
        List<Lecture> freshLectures = new ArrayList<>();

        for (Lecture l : sourceLectures) {
            freshLectures.add(toFreshLecture(l));
        }

        assignSectionNumbers(freshLectures);
        return lectureRepository.saveAll(freshLectures);
    }

    private Lecture toFreshLecture(Lecture source) {
        // Create a brand-new Lecture row (null id = INSERT not UPDATE)
        Lecture fresh = new Lecture();
        fresh.setId(null);

        if (source.getCourse() != null && source.getCourse().getId() != null) {
            fresh.setCourse(courseService.getEntityById(source.getCourse().getId()));
        }
        if (source.getRoom() != null && source.getRoom().getId() != null) {
            fresh.setRoom(roomService.getEntityById(source.getRoom().getId()));
        }
        if (source.getTimeSlot() != null && source.getTimeSlot().getId() != null) {
            fresh.setTimeSlot(timeSlotService.getEntityById(source.getTimeSlot().getId()));
        }
        if (source.getInstructor() != null && source.getInstructor().getId() != null) {
            fresh.setInstructor(
                    instructorRepository.findById(source.getInstructor().getId()).orElse(null)
            );
        }

        return fresh;
    }

    private void assignSectionNumbers(List<Lecture> lectures) {
        // Auto-assign section numbers per course (1, 2, 3… for each section of the same course)
        Map<Long, Integer> sectionCounters = new HashMap<>();
        for (Lecture fresh : lectures) {
            if (fresh.getCourse() != null) {
                Long courseId = fresh.getCourse().getId();
                int section = sectionCounters.merge(courseId, 1, Integer::sum);
                fresh.setSectionNumber(section);
            }
        }
    }
}