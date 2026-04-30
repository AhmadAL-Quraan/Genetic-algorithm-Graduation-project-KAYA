package com.kaya.controller;

import com.kaya.model.Lecture;
import com.kaya.model.TimeSlot;
import com.kaya.repository.LectureRepository;
import com.kaya.repository.TimeTableRepository;
import lombok.RequiredArgsConstructor;
import org.dhatim.fastexcel.Workbook;
import org.dhatim.fastexcel.Worksheet;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/export")
@RequiredArgsConstructor
public class ExportController {

    private final LectureRepository lectureRepository;
    private final TimeTableRepository timeTableRepository;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    @GetMapping("/schedule")
    public ResponseEntity<byte[]> exportSchedule() throws Exception {
        List<Lecture> lectures = lectureRepository.findAll().stream()
                .filter(l -> l.getTimeSlot() != null)
                .toList();
        return buildResponse(lectures, "KAYA_Schedule");
    }

    @GetMapping("/schedule/{timetableId}")
    public ResponseEntity<byte[]> exportTimetable(@PathVariable Long timetableId) throws Exception {
        var tt = timeTableRepository.findById(timetableId)
                .orElseThrow(() -> new RuntimeException("Timetable not found"));
        return buildResponse(tt.getLectures(), "KAYA_Timetable_" + timetableId);
    }

    private ResponseEntity<byte[]> buildResponse(List<Lecture> lectures, String filename) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (Workbook wb = new Workbook(out, "KAYA Scheduler", "1.0")) {
            Worksheet ws = wb.newWorksheet("Schedule");

            String[] headers = {"Course", "Section", "Instructor / Teacher", "Room",
                    "Days", "Start Time", "End Time", "Teaching Method", "Majors"};
            for (int col = 0; col < headers.length; col++) {
                ws.value(0, col, headers[col]);
                ws.style(0, col).bold().set();
            }

            int row = 1;
            for (Lecture l : lectures) {
                ws.value(row, 0, l.getCourse() != null
                        ? l.getCourse().getCourseSymbol() + " " + l.getCourse().getCourseNumber() : "");
                ws.value(row, 1, l.getSectionNumber() != null ? l.getSectionNumber().toString() : "1");
                String teacher = l.getTeacher() != null ? l.getTeacher().getName()
                        : (l.getInstructor() != null ? l.getInstructor() : "");
                ws.value(row, 2, teacher);
                ws.value(row, 3, l.getRoom() != null
                        ? l.getRoom().getBuilding() + " " + l.getRoom().getRoomNumber() : "");

                TimeSlot ts = l.getTimeSlot();
                if (ts != null) {
                    ws.value(row, 4, ts.getDays().toString().replaceAll("[\\[\\]]", ""));
                    ws.value(row, 5, ts.getStartTime().format(TIME_FMT));
                    ws.value(row, 6, ts.getEndTime().format(TIME_FMT));
                    ws.value(row, 7, ts.getTeachingMethod() != null ? ts.getTeachingMethod().name() : "");
                }
                ws.value(row, 8, l.getCourse() != null
                        ? String.join(", ", l.getCourse().getMajors()) : "");
                row++;
            }
        }

        byte[] bytes = out.toByteArray();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + ".xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .contentLength(bytes.length)
                .body(bytes);
    }
}
