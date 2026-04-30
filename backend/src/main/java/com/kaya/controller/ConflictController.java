package com.kaya.controller;

import com.kaya.model.Lecture;
import com.kaya.model.TimeSlot;
import com.kaya.repository.LectureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.time.DayOfWeek;
import java.util.*;

@RestController
@RequestMapping("/conflicts")
@RequiredArgsConstructor
public class ConflictController {

    private final LectureRepository lectureRepository;

    @GetMapping
    public List<Map<String, Object>> getConflicts() {
        List<Lecture> lectures = lectureRepository.findAll().stream()
                .filter(l -> l.getTimeSlot() != null)
                .toList();

        List<Map<String, Object>> conflicts = new ArrayList<>();

        for (int i = 0; i < lectures.size(); i++) {
            for (int j = i + 1; j < lectures.size(); j++) {
                Lecture a = lectures.get(i);
                Lecture b = lectures.get(j);

                if (!overlaps(a.getTimeSlot(), b.getTimeSlot())) continue;

                String instrA = resolveInstructor(a);
                String instrB = resolveInstructor(b);

                // Room conflict
                if (a.getRoom() != null && b.getRoom() != null
                        && a.getRoom().getId().equals(b.getRoom().getId())) {
                    conflicts.add(buildConflict("ROOM",
                            "Room conflict: " + a.getRoom().getBuilding() + " " + a.getRoom().getRoomNumber()
                                    + " is double-booked",
                            a, b));
                }

                // Teacher/instructor conflict
                if (instrA != null && instrB != null && !instrA.isBlank() && instrA.equalsIgnoreCase(instrB)) {
                    conflicts.add(buildConflict("TEACHER",
                            "Teacher conflict: " + instrA + " has two classes at the same time",
                            a, b));
                }

                // Student group (same major, overlapping courses)
                if (a.getCourse() != null && b.getCourse() != null) {
                    Set<String> majorsA = new HashSet<>(a.getCourse().getMajors());
                    Set<String> majorsB = new HashSet<>(b.getCourse().getMajors());
                    majorsA.retainAll(majorsB);
                    if (!majorsA.isEmpty()) {
                        conflicts.add(buildConflict("STUDENT",
                                "Student group conflict: majors " + majorsA + " have overlapping courses",
                                a, b));
                    }
                }
            }
        }
        return conflicts;
    }

    private boolean overlaps(TimeSlot a, TimeSlot b) {
        if (a == null || b == null) return false;
        Set<DayOfWeek> sharedDays = new HashSet<>(a.getDays());
        sharedDays.retainAll(b.getDays());
        if (sharedDays.isEmpty()) return false;
        return a.getStartTime().isBefore(b.getEndTime()) && b.getStartTime().isBefore(a.getEndTime());
    }

    private String resolveInstructor(Lecture l) {
        if (l.getTeacher() != null) return l.getTeacher().getName();
        return l.getInstructor();
    }

    private Map<String, Object> buildConflict(String type, String message, Lecture a, Lecture b) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("type", type);
        m.put("message", message);
        m.put("lectureAId", a.getId());
        m.put("lectureBId", b.getId());
        m.put("courseA", a.getCourse() != null ? a.getCourse().getCourseSymbol() + " " + a.getCourse().getCourseNumber() : "—");
        m.put("courseB", b.getCourse() != null ? b.getCourse().getCourseSymbol() + " " + b.getCourse().getCourseNumber() : "—");
        m.put("instructorA", resolveInstructor(a));
        m.put("instructorB", resolveInstructor(b));
        m.put("timeSlot", a.getTimeSlot().getStartTime() + " – " + a.getTimeSlot().getEndTime());
        return m;
    }
}
