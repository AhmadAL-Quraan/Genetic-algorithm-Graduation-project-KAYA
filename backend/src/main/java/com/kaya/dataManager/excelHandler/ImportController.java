//package com.kaya.dataManager.excelHandler;
//
//import com.kaya.model.*;
//import com.kaya.model.enums.RoomType;
//import com.kaya.model.enums.TeachingMethod;
//import com.kaya.repository.*;
//import lombok.RequiredArgsConstructor;
//import org.dhatim.fastexcel.reader.Cell;
//import org.dhatim.fastexcel.reader.ReadableWorkbook;
//import org.dhatim.fastexcel.reader.Row;
//import org.dhatim.fastexcel.reader.Sheet;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.time.DayOfWeek;
//import java.time.LocalTime;
//import java.util.*;
//import java.util.stream.Stream;
//
//@RestController
//@RequestMapping("/import")
//@RequiredArgsConstructor
//public class ImportController {
//
//    private final CourseRepository    courseRepository;
//    private final InstructorRepository   instructorRepository;
//    private final RoomRepository      roomRepository;
//    private final TimeSlotRepository  timeSlotRepository;
//    private final LectureRepository   lectureRepository;
//
//    @PostMapping(value = "/excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    @Transactional
//    public ResponseEntity<ImportSummary> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
//
//        // ── Preload existing data into lookup maps ─────────────────────────
//        Map<String, Course>   courseMap  = new HashMap<>();
//        Map<String, Instructor>  instructorMap = new HashMap<>();
//        Map<String, Room>     roomMap    = new HashMap<>();
//        Map<String, TimeSlot> slotMap    = new HashMap<>();
//
//        courseRepository .findAll().forEach(c -> courseMap .put(courseKey(c.getCourseSymbol(), c.getCourseNumber()), c));
//        instructorRepository.findAll().forEach(t -> instructorMap.put(normalize(t.getInstructorName()), t));
//        roomRepository   .findAll().forEach(r -> roomMap   .put(roomKey(r.getBuilding(), r.getRoomNumber()), r));
//        timeSlotRepository.findAll().forEach(ts -> {
//            if (ts.getDays() != null && ts.getStartTime() != null) {
//                slotMap.put(slotKey(ts), ts);
//            }
//        });
//
//        int rowsProcessed = 0, rowsSkipped = 0;
//        int coursesCreated = 0, instructorCreated = 0, roomsCreated = 0,
//            timeSlotsCreated = 0, lecturesCreated = 0;
//        List<String> warnings = new ArrayList<>();
//
//        try (ReadableWorkbook wb = new ReadableWorkbook(file.getInputStream())) {
//            Sheet sheet = wb.getFirstSheet();
//            try (Stream<Row> rows = sheet.openStream()) {
//                List<Row> rowList = rows.toList();
//
//                for (int i = 1; i < rowList.size(); i++) {
//                    Row row = rowList.get(i);
//                    rowsProcessed++;
//
//                    try {
//                        // ── Basic fields ──────────────────────────────────
//                        String symbol  = cell(row, 0);
//                        String number  = cell(row, 1);
//                        String section = cell(row, 2);
//
//                        if (symbol.isEmpty() || number.isEmpty()) { rowsSkipped++; continue; }
//
//                        // Skip canceled sections
//                        String cancelled = cell(row, 16);
//                        if (!cancelled.equals("لا") && !cancelled.isEmpty()) { rowsSkipped++; continue; }
//
//                        // ── Teaching method ───────────────────────────────
//                        String methodAr = cell(row, 20);
//                        TeachingMethod method = parseMethod(methodAr);
//
//                        // ── Days ──────────────────────────────────────────
//                        // cols 6=SAT, 7=SUN, 8=MON, 9=TUE, 10=WED, 11=THU, 12=FRI
//                        Set<DayOfWeek> days = new LinkedHashSet<>();
//                        if (isX(row, 6))  days.add(DayOfWeek.SATURDAY);
//                        if (isX(row, 7))  days.add(DayOfWeek.SUNDAY);
//                        if (isX(row, 8))  days.add(DayOfWeek.MONDAY);
//                        if (isX(row, 9))  days.add(DayOfWeek.TUESDAY);
//                        if (isX(row, 10)) days.add(DayOfWeek.WEDNESDAY);
//                        if (isX(row, 11)) days.add(DayOfWeek.THURSDAY);
//                        if (isX(row, 12)) days.add(DayOfWeek.FRIDAY);
//
//                        // ── Times ─────────────────────────────────────────
//                        LocalTime startTime = null, endTime = null;
//                        String startRaw = cell(row, 13);
//                        String endRaw   = cell(row, 14);
//                        if (!startRaw.isEmpty() && !endRaw.isEmpty()) {
//                            try {
//                                startTime = excelFractionToTime(Double.parseDouble(startRaw));
//                                endTime   = excelFractionToTime(Double.parseDouble(endRaw));
//                            } catch (NumberFormatException ignored) {}
//                        }
//
//                        // ── Room ──────────────────────────────────────────
//                        String roomCode = cell(row, 17);
//                        Room room = null;
//                        boolean isOnline = roomCode.isEmpty()
//                                || roomCode.toLowerCase().contains("oline")
//                                || roomCode.toLowerCase().contains("online")
//                                || roomCode.startsWith("0 ");
//
//                        if (!isOnline && !roomCode.isEmpty()) {
//                            String[] parts = roomCode.trim().split("\\s+", 2);
//                            String building = parts[0];
//                            String roomNum  = parts.length > 1 ? parts[1] : parts[0];
//                            String rk = roomKey(building, roomNum);
//                            room = roomMap.get(rk);
//                            if (room == null) {
//                                room = new Room(null, building, roomNum, RoomType.LECTURE);
//                                room = roomRepository.save(room);
//                                roomMap.put(rk, room);
//                                roomsCreated++;
//                            }
//                        }
//
//                        // ── Instructor ──────────────────────────
//                        String instructorName = cell(row, 19);
//                        Instructor instructor = null;
//                        if (!instructorName.isEmpty()) {
//                            String nk = normalize(instructorName);
//                            instructor = instructorMap.get(nk);
//                            if (instructor == null) {
//                                instructor = new Instructor(null, instructorName, null, null);
//                                instructor = instructorRepository.save(instructor);
//                                instructorMap.put(nk, instructor);
//                                instructorCreated++;
//                            }
//                        }
//
//                        // ── Course ────────────────────────────────────────
//                        RoomType roomType = (room != null) ? room.getRoomType() : RoomType.LECTURE;
//                        String ck = courseKey(symbol, number);
//                        Course course = courseMap.get(ck);
//                        if (course == null) {
//                            course = new Course(symbol, number, roomType, method);
//                            course = courseRepository.save(course);
//                            courseMap.put(ck, course);
//                            coursesCreated++;
//                        }
//
//                        // ── TimeSlot ──────────────────────────────────────
//                        TimeSlot slot = null;
//                        if (startTime != null && endTime != null && !days.isEmpty()) {
//                            TimeSlot probe = new TimeSlot(null, startTime, endTime, days, method, null);
//                            String sk = slotKey(probe);
//                            slot = slotMap.get(sk);
//                            if (slot == null) {
//                                slot = new TimeSlot(null, startTime, endTime, days, method, null);
//                                slot = timeSlotRepository.save(slot);
//                                slotMap.put(sk, slot);
//                                timeSlotsCreated++;
//                            }
//                        }
//
//                        // ── Lecture (template) ────────────────────────────
//                        Integer sectionNum = null;
//                        try { sectionNum = Integer.parseInt(section); } catch (NumberFormatException ignored) {}
//
//                        Lecture lecture = new Lecture();
//                        lecture.setCourse(course);
//                        lecture.setRoom(room);
//                        lecture.setTimeSlot(slot);
//                        lecture.setSectionNumber(sectionNum);
//                        lecture.setInstructor(instructor);
//                        lecture.setInstructor(instructorName.isEmpty() ? null : instructor);
//                        lectureRepository.save(lecture);
//                        lecturesCreated++;
//
//                    } catch (Exception e) {
//                        warnings.add("Row " + (i + 1) + ": " + e.getMessage());
//                        rowsSkipped++;
//                    }
//                }
//            }
//        }
//
//        return ResponseEntity.ok(new ImportSummary(
//                rowsProcessed, rowsSkipped,
//                coursesCreated, roomsCreated, timeSlotsCreated,
//                lecturesCreated, instructorCreated, warnings));
//    }
//
//    // ── Helpers ──────────────────────────────────────────────────────────────
//
//    private String cell(Row row, int col) {
//        if (col >= row.getCellCount()) return "";
//        Cell c = row.getCell(col);
//        if (c == null || c.getValue() == null) return "";
//        return c.getValue().toString().trim();
//    }
//
//    private boolean isX(Row row, int col) {
//        return cell(row, col).equalsIgnoreCase("X") || cell(row, col).equals("×");
//    }
//
//    private String normalize(String s) {
//        return s == null ? "" : s.trim().toLowerCase();
//    }
//
//    private String courseKey(String symbol, String number) {
//        return normalize(symbol) + "|" + normalize(number);
//    }
//
//    private String roomKey(String building, String number) {
//        return normalize(building) + "|" + normalize(number);
//    }
//
//    private String slotKey(TimeSlot ts) {
//        // Sort days for consistent key
//        List<String> days = ts.getDays().stream()
//                .sorted(Comparator.comparingInt(DayOfWeek::getValue))
//                .map(Enum::name)
//                .toList();
//        String method = ts.getTeachingMethod() != null ? ts.getTeachingMethod().name() : "null";
//        return ts.getStartTime() + "-" + ts.getEndTime() + "-" + days + "-" + method;
//    }
//
//    private TeachingMethod parseMethod(String arabic) {
//        if (arabic == null) return TeachingMethod.IN_PERSON;
//        return switch (arabic.trim()) {
//            case "إلكتروني", "الكتروني", "أونلاين", "اونلاين" -> TeachingMethod.ONLINE;
//            case "مدمج", "هجين"                               -> TeachingMethod.BLENDED;
//            default                                            -> TeachingMethod.IN_PERSON;
//        };
//    }
//
//    private LocalTime excelFractionToTime(double fraction) {
//        int totalMinutes = (int) Math.round(fraction * 24 * 60);
//        return LocalTime.of(totalMinutes / 60, totalMinutes % 60);
//    }
//
//    // ── Summary DTO ──────────────────────────────────────────────────────────
//
//    public record ImportSummary(
//            int rowsProcessed, int rowsSkipped,
//            int coursesCreated, int roomsCreated, int timeSlotsCreated,
//            int lecturesCreated, int instructorCreated,
//            List<String> warnings) {}
//}
