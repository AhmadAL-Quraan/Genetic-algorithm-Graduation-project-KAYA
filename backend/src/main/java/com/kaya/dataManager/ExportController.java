package com.kaya.dataManager;

import com.kaya.model.Course;
import com.kaya.model.Lecture;
import com.kaya.model.TimeTable;
import com.kaya.repository.CourseRepository;
import com.kaya.repository.TimeTableRepository;
import lombok.RequiredArgsConstructor;
import org.dhatim.fastexcel.Color;
import org.dhatim.fastexcel.Workbook;
import org.dhatim.fastexcel.Worksheet;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@RestController
@RequestMapping("/export")
@RequiredArgsConstructor
public class ExportController {

    private final TimeTableRepository timeTableRepository;
    private final CourseRepository courseRepository;

    // ── Day-pattern groups (Yarmouk University) ───────────────────────────────
    private static final Set<DayOfWeek> STT = Set.of(DayOfWeek.SUNDAY, DayOfWeek.TUESDAY, DayOfWeek.THURSDAY);
    private static final Set<DayOfWeek> MWS = Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.SATURDAY);
    private static final Set<DayOfWeek> MW  = Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY);

    /** Export the latest generated timetable. */
    @GetMapping("/schedule/latest")
    public ResponseEntity<byte[]> exportLatest() throws Exception {
        TimeTable tt = timeTableRepository.findAll().stream()
                .max(Comparator.comparingLong(t -> t.getId() == null ? 0L : t.getId()))
                .orElseThrow(() -> new IllegalStateException("No timetables found"));
        return buildExport(tt);
    }

    /** Export a specific generated timetable as Excel. */
    @GetMapping("/schedule/{id}")
    public ResponseEntity<byte[]> exportTimetable(@PathVariable Long id) throws Exception {
        TimeTable tt = timeTableRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Timetable not found: " + id));
        return buildExport(tt);
    }

    /** Export the full course list as Excel (used from the Courses page). */
    @GetMapping("/schedule")
    public ResponseEntity<byte[]> exportCourses() throws Exception {
        List<Course> courses = courseRepository.findAll();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (Workbook wb = new Workbook(out, "KAYA Scheduler", "1.0")) {
            Worksheet ws = wb.newWorksheet("Courses");

            String[] headers = {"#", "Course Symbol", "Course Number", "Room Type", "Teaching Method"};
            for (int c = 0; c < headers.length; c++) {
                ws.value(0, c, headers[c]);
                ws.style(0, c).bold().set();
            }

            for (int r = 0; r < courses.size(); r++) {
                Course co = courses.get(r);
                ws.value(r + 1, 0, r + 1);
                ws.value(r + 1, 1, co.getCourseSymbol() != null ? co.getCourseSymbol() : "");
                ws.value(r + 1, 2, co.getCourseNumber() != null ? co.getCourseNumber() : "");
                ws.value(r + 1, 3, co.getRequiredRoomType() != null ? co.getRequiredRoomType().name() : "");
                ws.value(r + 1, 4, co.getTeachingMethod() != null ? co.getTeachingMethod().name() : "");
            }
        }

        return xlsxResponse(out, "courses.xlsx");
    }

    // ── Core export builder ───────────────────────────────────────────────────

    private ResponseEntity<byte[]> buildExport(TimeTable tt) throws Exception {

        // Sort lectures: group → start time → course symbol
        List<Lecture> scheduled = tt.getLectures().stream()
                .filter(l -> l.getTimeSlot() != null && l.getCourse() != null)
                .sorted(Comparator
                        .comparingInt((Lecture l) -> groupOf(l.getTimeSlot().getDays()))
                        .thenComparing(l -> l.getTimeSlot().getStartTime())
                        .thenComparing(l -> courseCode(l.getCourse())))
                .toList();

        List<Lecture> unscheduled = tt.getLectures().stream()
                .filter(l -> l.getTimeSlot() == null && l.getCourse() != null)
                .sorted(Comparator.comparing(l -> courseCode(l.getCourse())))
                .toList();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (Workbook wb = new Workbook(out, "KAYA Scheduler", "1.0")) {
            Worksheet ws = wb.newWorksheet("Timetable");

            // ── Row 0: Title ───────────────────────────────────────────────
            ws.value(0, 0, "KAYA \u2013 University Course Timetable System (Yarmouk University)");
            ws.style(0, 0)
                    .bold()
                    .fontSize(14)
                    .fontColor(Color.WHITE)
                    .fillColor("1F3864")
                    .set();

            // ── Row 1: Subtitle ────────────────────────────────────────────
            ws.value(1, 0, "Generated by: KAYA Genetic-Algorithm Scheduler");
            ws.style(1, 0).italic().fontColor("595959").set();

            // ── Row 2: Blank ───────────────────────────────────────────────

            // ── Row 3: Sorting header ──────────────────────────────────────
            ws.value(3, 0, "HOW THIS SHEET IS SORTED:");
            ws.style(3, 0).bold().set();

            // ── Rows 4–8: Group descriptions ──────────────────────────────
            ws.value(4, 0, "  Group 1 \u2014 Sunday / Tuesday / Thursday (STT pattern)  \u2192  courses that meet on Sun, Tue, Thu");
            ws.value(5, 0, "  Group 2 \u2014 Monday / Wednesday / Saturday (MWS pattern)  \u2192  courses that meet on Mon, Wed, Sat");
            ws.value(6, 0, "  Group 3 \u2014 Monday / Wednesday (MW pattern)  \u2192  courses that meet on Mon, Wed only");
            ws.value(7, 0, "  Group 4 \u2014 All other day combinations (listed last)");
            ws.value(8, 0, "  Within each group, courses are ordered by Start Time (earliest first), then alphabetically by Course code.");

            // ── Row 9: Blank ───────────────────────────────────────────────

            // ── Row 10: Column headers ─────────────────────────────────────
            int headerRow = 10;
            // Columns: Course | Section | Instructor/Teacher | Room | Days | Start Time | End Time | Teaching Method | Majors
            String[] cols = {"Course", "Section", "Instructor / Teacher", "Room", "Days", "Start Time", "End Time", "Teaching Method", "Majors"};
            for (int c = 0; c < cols.length; c++) {
                ws.value(headerRow, c, cols[c]);
                ws.style(headerRow, c)
                        .bold()
                        .fontColor(Color.WHITE)
                        .fillColor("2E4057")
                        .set();
            }

            // ── Rows 11+: Data ─────────────────────────────────────────────
            int currentGroup = -1;
            int seq = 0;
            for (Lecture lec : scheduled) {
                int group = groupOf(lec.getTimeSlot().getDays());

                // Insert a blank separator row between groups
                if (group != currentGroup && currentGroup != -1) {
                    seq++; // blank gap row
                }
                currentGroup = group;

                int row = headerRow + 1 + seq;
                seq++;

                // Col 0: Course = "SYMBOL NUMBER"
                String courseName = (lec.getCourse().getCourseSymbol() != null ? lec.getCourse().getCourseSymbol() : "")
                        + " " + (lec.getCourse().getCourseNumber() != null ? lec.getCourse().getCourseNumber() : "");
                ws.value(row, 0, courseName.trim());

                // Col 1: Section
                ws.value(row, 1, lec.getSectionNumber() != null ? lec.getSectionNumber() : 0);

                // Col 2: Instructor / Teacher
                ws.value(row, 2, lec.getInstructor().getInstructorName() != null ? lec.getInstructor().getInstructorName() : "");

                // Col 3: Room (number only)
                ws.value(row, 3, lec.getRoom() != null ? lec.getRoom().getRoomNumber() : "");

                // Col 4: Days — expand single-day GA slots back to their full group (STT / MW / MWS)
                ws.value(row, 4, formatDaysFull(canonicalGroup(lec.getTimeSlot().getDays())));

                // Col 5: Start Time
                ws.value(row, 5, lec.getTimeSlot().getStartTime().toString());

                // Col 6: End Time
                ws.value(row, 6, lec.getTimeSlot().getEndTime().toString());

                // Col 7: Teaching Method
                String method = lec.getCourse().getTeachingMethod() != null
                        ? lec.getCourse().getTeachingMethod().name() : "";
                ws.value(row, 7, method);

                // Col 8: Majors (left empty — filled by registrar)
                ws.value(row, 8, "");

                // Alternate row shading
                if (seq % 2 == 0) {
                    for (int c = 0; c < cols.length; c++) {
                        ws.style(row, c).fillColor("F2F2F2").set();
                    }
                }
            }

            // ── Column widths ──────────────────────────────────────────────
            ws.width(0, 14);  // Course
            ws.width(1, 9);   // Section
            ws.width(2, 28);  // Instructor / Teacher
            ws.width(3, 8);   // Room
            ws.width(4, 36);  // Days
            ws.width(5, 12);  // Start Time
            ws.width(6, 12);  // End Time
            ws.width(7, 18);  // Teaching Method
            ws.width(8, 14);  // Majors

            // ── Unscheduled sheet ──────────────────────────────────────────
            if (!unscheduled.isEmpty()) {
                Worksheet ws2 = wb.newWorksheet("Unscheduled");
                String[] uh = {"#", "Symbol", "Number", "Section", "Instructor"};
                for (int c = 0; c < uh.length; c++) {
                    ws2.value(0, c, uh[c]);
                    ws2.style(0, c).bold().fillColor("C00000").fontColor(Color.WHITE).set();
                }
                for (int r = 0; r < unscheduled.size(); r++) {
                    Lecture lec = unscheduled.get(r);
                    ws2.value(r + 1, 0, r + 1);
                    ws2.value(r + 1, 1, lec.getCourse().getCourseSymbol() != null ? lec.getCourse().getCourseSymbol() : "");
                    ws2.value(r + 1, 2, lec.getCourse().getCourseNumber() != null ? lec.getCourse().getCourseNumber() : "");
                    ws2.value(r + 1, 3, lec.getSectionNumber() != null ? String.valueOf(lec.getSectionNumber()) : "");
                    ws2.value(r + 1, 4, lec.getInstructor().getInstructorName() != null ? lec.getInstructor().getInstructorName() : "");
                }
            }
        }

        return xlsxResponse(out, "KAYA_Timetable_" + tt.getId() + ".xlsx");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * The GA expands windows into per-day individual slots (e.g. {SUNDAY}).
     * For display purposes we map those single-day (or partial) sets back to
     * their full canonical Yarmouk group so the export shows the complete
     * day pattern a course actually meets (STT / MW / MWS).
     */
    private Set<DayOfWeek> canonicalGroup(Set<DayOfWeek> days) {
        if (days == null || days.isEmpty()) return days;
        if (days.equals(STT) || days.equals(MWS) || days.equals(MW)) return days;
        // subset of STT (SUN, TUE, THU)
        if (STT.containsAll(days) && days.stream().allMatch(STT::contains)) return STT;
        // subset of MWS that includes SAT
        if (days.contains(DayOfWeek.SATURDAY) && MWS.containsAll(days)) return MWS;
        // subset of MW (MON, WED)
        if (MW.containsAll(days) && days.stream().allMatch(MW::contains)) return MW;
        return days;
    }

    private int groupOf(Set<DayOfWeek> days) {
        Set<DayOfWeek> g = canonicalGroup(days);
        if (g == null || g.isEmpty()) return 4;
        if (g.equals(STT)) return 1;
        if (g.equals(MWS)) return 2;
        if (g.equals(MW))  return 3;
        return 4;
    }

    private String patternLabel(Set<DayOfWeek> days) {
        Set<DayOfWeek> g = canonicalGroup(days);
        if (g == null || g.isEmpty()) return "";
        if (g.equals(STT)) return "STT";
        if (g.equals(MWS)) return "MWS";
        if (g.equals(MW))  return "MW";
        return "Other";
    }

    private String courseCode(Course c) {
        if (c == null) return "";
        return (c.getCourseSymbol() != null ? c.getCourseSymbol() : "") +
               (c.getCourseNumber() != null ? c.getCourseNumber() : "");
    }

    /** Short names for internal use (e.g. "Sat / Sun"). */
    private String formatDays(Set<DayOfWeek> days) {
        if (days == null || days.isEmpty()) return "";
        List<DayOfWeek> order = List.of(
                DayOfWeek.SATURDAY, DayOfWeek.SUNDAY, DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);
        return order.stream()
                .filter(days::contains)
                .map(d -> d.getDisplayName(TextStyle.SHORT, Locale.ENGLISH).toUpperCase(Locale.ENGLISH))
                .reduce((a, b) -> a + " / " + b)
                .orElse("");
    }

    /** Full names for the Excel data column (e.g. "SUNDAY, TUESDAY, THURSDAY"). */
    private String formatDaysFull(Set<DayOfWeek> days) {
        if (days == null || days.isEmpty()) return "";
        List<DayOfWeek> order = List.of(
                DayOfWeek.SATURDAY, DayOfWeek.SUNDAY, DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);
        return order.stream()
                .filter(days::contains)
                .map(d -> d.getDisplayName(TextStyle.FULL, Locale.ENGLISH).toUpperCase(Locale.ENGLISH))
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
    }

    private String methodLabel(String method) {
        return switch (method) {
            case "IN_PERSON" -> "In Person";
            case "ONLINE"    -> "Online";
            case "BLENDED"   -> "Blended";
            default          -> method;
        };
    }

    private ResponseEntity<byte[]> xlsxResponse(ByteArrayOutputStream out, String filename) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(out.toByteArray());
    }
}
