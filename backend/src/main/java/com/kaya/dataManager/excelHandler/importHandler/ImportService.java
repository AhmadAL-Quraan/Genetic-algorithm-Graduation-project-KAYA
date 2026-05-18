package com.kaya.dataManager.excelHandler.importHandler;

import com.kaya.model.*;
import com.kaya.model.enums.RoomType;
import com.kaya.model.enums.TeachingMethod;
import com.kaya.repository.*;
import lombok.RequiredArgsConstructor;
import org.dhatim.fastexcel.reader.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ImportService {

    private final CourseRepository courseRepository;
    private final InstructorRepository instructorRepository;
    private final RoomRepository roomRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final LectureRepository lectureRepository;
    private final ImportHelper helper;

    @Transactional
    public ImportSummary importExcel(MultipartFile file) throws Exception {
        // ── Preload existing data into lookup maps ─────────────────────────
        Map<String, Course>   courseMap  = new HashMap<>();
        Map<String, Instructor>  instructorMap = new HashMap<>();
        Map<String, Room>     roomMap    = new HashMap<>();
        Map<String, TimeSlot> slotMap    = new HashMap<>();

        courseRepository .findAll().forEach(c -> courseMap .put(helper.courseKey(c.getCourseSymbol(), c.getCourseNumber()), c));
        instructorRepository.findAll().forEach(t -> instructorMap.put(helper.normalize(t.getInstructorName()), t));
        roomRepository   .findAll().forEach(r -> roomMap   .put(helper.roomKey(r.getBuilding(), r.getRoomNumber()), r));
        timeSlotRepository.findAll().forEach(ts -> {
            if (ts.getDays() != null && ts.getStartTime() != null) {
                slotMap.put(helper.slotKey(ts), ts);
            }
        });

        int rowsProcessed = 0, rowsSkipped = 0;
        int coursesCreated = 0, instructorsCreated = 0, roomsCreated = 0,
                timeSlotsCreated = 0, lecturesCreated = 0;
        List<String> warnings = new ArrayList<>();

        try (ReadableWorkbook wb = new ReadableWorkbook(file.getInputStream())) {
            Sheet sheet = wb.getFirstSheet();
            try (Stream<Row> rows = sheet.openStream()) {
                List<Row> rowList = rows.toList();

                for (int i = 1; i < rowList.size(); i++) {
                    Row row = rowList.get(i);
                    rowsProcessed++;

                    try {
                        // ── Basic fields ──────────────────────────────────
                        String symbol  = helper.cell(row, 0);
                        String number  = helper.cell(row, 1);
                        String section = helper.cell(row, 2);

                        if (symbol.isEmpty() || number.isEmpty()) { rowsSkipped++; continue; }

                        // Skip cancelled sections
                        String cancelled = helper.cell(row, 16);
                        if (!cancelled.equals("لا") && !cancelled.isEmpty()) { rowsSkipped++; continue; }

                        // ── Teaching method ───────────────────────────────
                        String methodAr = helper.cell(row, 20);
                        TeachingMethod method = helper.parseMethod(methodAr);

                        // ── Days ──────────────────────────────────────────
                        // cols 6=SAT, 7=SUN, 8=MON, 9=TUE, 10=WED, 11=THU, 12=FRI
                        Set<DayOfWeek> days = new LinkedHashSet<>();
                        if (helper.isX(row, 6))  days.add(DayOfWeek.SATURDAY);
                        if (helper.isX(row, 7))  days.add(DayOfWeek.SUNDAY);
                        if (helper.isX(row, 8))  days.add(DayOfWeek.MONDAY);
                        if (helper.isX(row, 9))  days.add(DayOfWeek.TUESDAY);
                        if (helper.isX(row, 10)) days.add(DayOfWeek.WEDNESDAY);
                        if (helper.isX(row, 11)) days.add(DayOfWeek.THURSDAY);
                        if (helper.isX(row, 12)) days.add(DayOfWeek.FRIDAY);

                        // ── Times ─────────────────────────────────────────
                        LocalTime startTime = null, endTime = null;
                        String startRaw = helper.cell(row, 13);
                        String endRaw   = helper.cell(row, 14);
                        if (!startRaw.isEmpty() && !endRaw.isEmpty()) {
                            try {
                                startTime = helper.excelFractionToTime(Double.parseDouble(startRaw));
                                endTime   = helper.excelFractionToTime(Double.parseDouble(endRaw));
                            } catch (NumberFormatException ignored) {}
                        }

                        // ── Room ──────────────────────────────────────────
                        String roomCode = helper.cell(row, 17);
                        Room room = null;
                        boolean isOnline = roomCode.isEmpty()
                                || roomCode.toLowerCase().contains("oline")
                                || roomCode.toLowerCase().contains("online")
                                || roomCode.startsWith("0 ");

                        if (!isOnline && !roomCode.isEmpty()) {
                            String[] parts = roomCode.trim().split("\\s+", 2);
                            String building = parts[0];
                            String roomNum  = parts.length > 1 ? parts[1] : parts[0];
                            String rk = helper.roomKey(building, roomNum);
                            room = roomMap.get(rk);
                            if (room == null) {
                                room = new Room(null, building, roomNum, RoomType.LECTURE);
                                room = roomRepository.save(room);
                                roomMap.put(rk, room);
                                roomsCreated++;
                            }
                        }

                        // ── Instructor ──────────────────────────
                        String instructorName = helper.cell(row, 19);
                        Instructor instructor = null;
                        if (!instructorName.isEmpty()) {
                            String nk = helper.normalize(instructorName);
                            instructor = instructorMap.get(nk);
                            if (instructor == null) {
                                instructor = new Instructor(null, instructorName);
                                instructor = instructorRepository.save(instructor);
                                instructorMap.put(nk, instructor);
                                instructorsCreated++;
                            }
                        }

                        // ── Course ────────────────────────────────────────
                        RoomType roomType = (room != null) ? room.getRoomType() : RoomType.LECTURE;
                        String ck = helper.courseKey(symbol, number);
                        Course course = courseMap.get(ck);
                        if (course == null) {
                            course = new Course(symbol, number, roomType, method);
                            course = courseRepository.save(course);
                            courseMap.put(ck, course);
                            coursesCreated++;
                        }

                        // ── TimeSlot ──────────────────────────────────────
                        TimeSlot slot = null;
                        if (startTime != null && endTime != null && !days.isEmpty()) {
                            TimeSlot probe = new TimeSlot(null, startTime, endTime, days, method, null);
                            String sk = helper.slotKey(probe);
                            slot = slotMap.get(sk);
                            if (slot == null) {
                                slot = new TimeSlot(null, startTime, endTime, days, method, null);
                                slot = timeSlotRepository.save(slot);
                                slotMap.put(sk, slot);
                                timeSlotsCreated++;
                            }
                        }

                        // ── Lecture (template) ────────────────────────────
                        Integer sectionNum = null;
                        try { sectionNum = Integer.parseInt(section); } catch (NumberFormatException ignored) {}

                        Lecture lecture = new Lecture();
                        lecture.setCourse(course);
                        lecture.setRoom(room);
                        lecture.setTimeSlot(slot);
                        lecture.setSectionNumber(sectionNum);
                        lecture.setInstructor(instructor);
                        lecture.setInstructor(instructorName.isEmpty() ? null : instructor);
                        lectureRepository.save(lecture);
                        lecturesCreated++;

                    } catch (Exception e) {
                        warnings.add("Row " + (i + 1) + ": " + e.getMessage());
                        rowsSkipped++;
                    }
                }
            }
        }

        return new ImportSummary(
                rowsProcessed, rowsSkipped,
                coursesCreated, roomsCreated, timeSlotsCreated,
                lecturesCreated, instructorsCreated, warnings);
    }
}