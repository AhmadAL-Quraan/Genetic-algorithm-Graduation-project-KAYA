package com.kaya.service;

import com.kaya.dto.response.ConflictResponse;
import com.kaya.model.Lecture;
import com.kaya.model.TimeSlot;
import com.kaya.model.TimeTable;
import com.kaya.repository.TimeTableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ConflictService {

    private final TimeTableRepository timeTableRepository;

    @Transactional(readOnly = true)
    public List<ConflictResponse> getLatestConflicts() {
        List<TimeTable> all = timeTableRepository.findAll();
        if (all.isEmpty()) return List.of();

        TimeTable latest = all.stream()
                .max(Comparator.comparingLong(t -> t.getId() != null ? t.getId() : 0L))
                .orElse(null);
        if (latest == null) return List.of();

        List<Lecture> lectures = latest.getLectures();
        if (lectures == null || lectures.isEmpty()) return List.of();

        List<ConflictResponse> results = new ArrayList<>();

        for (int i = 0; i < lectures.size(); i++) {
            for (int j = i + 1; j < lectures.size(); j++) {
                Lecture a = lectures.get(i);
                Lecture b = lectures.get(j);

                if (!timeSlotsOverlap(a.getTimeSlot(), b.getTimeSlot())) continue;

                List<String> types = detectTypes(a, b);
                for (String type : types) {
                    ConflictResponse dto = new ConflictResponse(
                            type,
                            buildMessage(a, b, type),
                            a.getId(),
                            b.getId(),
                            courseName(a),
                            courseName(b),
                            instructorLabel(a),
                            instructorLabel(b),
                            formatTimeSlot(a.getTimeSlot())
                    );
                    results.add(dto);
                }
            }
        }
        return results;
    }

    // --- Helper Methods (Private Business Logic) ---

    private boolean timeSlotsOverlap(TimeSlot ts1, TimeSlot ts2) {
        if (ts1 == null || ts2 == null) return false;
        boolean dayOverlap = false;
        for (DayOfWeek day : ts1.getDays()) {
            if (ts2.getDays().contains(day)) { dayOverlap = true; break; }
        }
        if (!dayOverlap) return false;
        return ts1.getStartTime().isBefore(ts2.getEndTime())
                && ts2.getStartTime().isBefore(ts1.getEndTime());
    }

    private List<String> detectTypes(Lecture a, Lecture b) {
        List<String> types = new ArrayList<>();

        if (a.getRoom() != null && b.getRoom() != null
                && Objects.equals(a.getRoom().getId(), b.getRoom().getId())) {
            types.add("ROOM");
        }

        boolean sameInstructorStr = a.getInstructor() != null && b.getInstructor() != null
                && Objects.equals(a.getInstructor(), b.getInstructor());
        boolean sameTeacherFK = a.getInstructor() != null && b.getInstructor() != null
                && Objects.equals(a.getInstructor().getId(), b.getInstructor().getId());
        if (sameInstructorStr || sameTeacherFK) {
            types.add("INSTRUCTOR");
        }

        String groupA = studentGroup(a);
        String groupB = studentGroup(b);
        if (groupA != null && groupA.equals(groupB)) {
            types.add("STUDENT");
        }

        return types;
    }

    private String studentGroup(Lecture l) {
        if (l.getCourse() == null) return null;
        String sym = l.getCourse().getCourseSymbol();
        String num = l.getCourse().getCourseNumber();
        if (sym == null || sym.isBlank() || num == null || num.isBlank()) return null;
        return sym.trim() + "-" + num.charAt(0);
    }

    private String courseName(Lecture l) {
        if (l.getCourse() == null) return "";
        return l.getCourse().getCourseSymbol() + " " + l.getCourse().getCourseNumber();
    }

    private String instructorLabel(Lecture l) {
        if (l.getInstructor().getInstructorName() != null) return l.getInstructor().getInstructorName();
        return "null";
    }

    private String buildMessage(Lecture a, Lecture b, String type) {
        String courseA = courseName(a);
        String courseB = courseName(b);
        return switch (type) {
            case "ROOM"    -> "Room conflict: " + courseA + " and " + courseB
                    + " share room " + (a.getRoom() != null ? a.getRoom().getRoomNumber() : "?");
            case "INSTRUCTOR" -> "Instructor conflict: " + instructorLabel(a)
                    + " is teaching " + courseA + " and " + courseB + " simultaneously";
            case "STUDENT" -> "Student conflict: " + courseA + " and " + courseB
                    + " overlap for the same student group (" + studentGroup(a) + ")";
            default        -> type + " conflict between " + courseA + " and " + courseB;
        };
    }

    private String formatTimeSlot(TimeSlot ts) {
        if (ts == null) return "";
        return ts.getDays() + " " + ts.getStartTime() + "-" + ts.getEndTime();
    }
}