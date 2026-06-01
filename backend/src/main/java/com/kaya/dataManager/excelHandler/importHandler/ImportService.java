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

    // Define column indices as constants for readability and maintainability
    private static final int COL_SYMBOL = 0;
    private static final int COL_NUMBER = 1;
    private static final int COL_SECTION = 2;
    private static final int COL_SAT = 6;
    private static final int COL_SUN = 7;
    private static final int COL_MON = 8;
    private static final int COL_TUE = 9;
    private static final int COL_WED = 10;
    private static final int COL_THU = 11;
    private static final int COL_FRI = 12;
    private static final int COL_START_TIME = 13;
    private static final int COL_END_TIME = 14;
    private static final int COL_CANCELLED = 16;
    private static final int COL_ROOM_CODE = 17;
    private static final int COL_INSTRUCTOR_NAME = 19;
    private static final int COL_TEACHING_METHOD = 20;

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

        // Lists to hold new entities for batch saving
        List<Course> newCourses = new ArrayList<>();
        List<Instructor> newInstructors = new ArrayList<>();
        List<Room> newRooms = new ArrayList<>();
//        List<TimeSlot> newTimeSlots = new ArrayList<>();
        List<Lecture> newLectures = new ArrayList<>();

        int rowsProcessed = 0, rowsSkipped = 0;
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
                        String symbol  = helper.cell(row, COL_SYMBOL);
                        String number  = helper.cell(row, COL_NUMBER);
                        String section = helper.cell(row, COL_SECTION);

                        if (symbol.isEmpty() || number.isEmpty()) { rowsSkipped++; continue; }

                        // Skip canceled sections
                        String cancelled = helper.cell(row, COL_CANCELLED);
                        if (!cancelled.equals("لا") && !cancelled.isEmpty()) { rowsSkipped++; continue; }

                        // ── Teaching method ───────────────────────────────
                        String methodAr = helper.cell(row, COL_TEACHING_METHOD);
                        TeachingMethod method = helper.parseMethod(methodAr);

                        // ── Days ──────────────────────────────────────────
                        Set<DayOfWeek> days = new LinkedHashSet<>();
                        if (helper.isX(row, COL_SAT))  days.add(DayOfWeek.SATURDAY);
                        if (helper.isX(row, COL_SUN))  days.add(DayOfWeek.SUNDAY);
                        if (helper.isX(row, COL_MON))  days.add(DayOfWeek.MONDAY);
                        if (helper.isX(row, COL_TUE))  days.add(DayOfWeek.TUESDAY);
                        if (helper.isX(row, COL_WED)) days.add(DayOfWeek.WEDNESDAY);
                        if (helper.isX(row, COL_THU)) days.add(DayOfWeek.THURSDAY);
                        if (helper.isX(row, COL_FRI)) days.add(DayOfWeek.FRIDAY);

                        // ── Times ─────────────────────────────────────────
                        LocalTime startTime = null, endTime = null;
                        String startRaw = helper.cell(row, COL_START_TIME);
                        String endRaw   = helper.cell(row, COL_END_TIME);
                        if (!startRaw.isEmpty() && !endRaw.isEmpty()) {
                            try {
                                startTime = helper.excelFractionToTime(Double.parseDouble(startRaw));
                                endTime   = helper.excelFractionToTime(Double.parseDouble(endRaw));
                            } catch (NumberFormatException ignored) {}
                        }

                        // ── Room ──────────────────────────────────────────
                        String roomCode = helper.cell(row, COL_ROOM_CODE);
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
                                newRooms.add(room); // Add to list for batch save
                                roomMap.put(rk, room);
                            }
                        }

                        // ── Instructor ──────────────────────────
                        String instructorName = helper.cell(row, COL_INSTRUCTOR_NAME);
                        Instructor instructor = null;
                        if (!instructorName.isEmpty()) {
                            String nk = helper.normalize(instructorName);
                            instructor = instructorMap.get(nk);
                            if (instructor == null) {
                                instructor = new Instructor(null, instructorName,null,null);
                                newInstructors.add(instructor); // Add to list for batch save
                                instructorMap.put(nk, instructor);
                            }
                        }

                        // ── Course ────────────────────────────────────────
                        RoomType roomType = (room != null) ? room.getRoomType() : RoomType.LECTURE;
                        String ck = helper.courseKey(symbol, number);
                        Course course = courseMap.get(ck);
                        if (course == null) {
                            course = new Course(symbol, number, roomType, method);
                            newCourses.add(course); // Add to list for batch save
                            courseMap.put(ck, course);
                        }

                        // ── TimeSlot ──────────────────────────────────────
//                        TimeSlot slot = null;
//                        if (startTime != null && endTime != null && !days.isEmpty()) {
//                            TimeSlot probe = new TimeSlot(null, startTime, endTime, days, method, null);
//                            String sk = helper.slotKey(probe);
//                            slot = slotMap.get(sk);
//                            if (slot == null) {
//                                slot = new TimeSlot(null, startTime, endTime, days, method, null);
//                                newTimeSlots.add(slot); // Add to list for batch save
//                                slotMap.put(sk, slot);
//                            }
//                        }

                        // ── Lecture (template) ────────────────────────────
                        Integer sectionNum = null;
                        try { sectionNum = Integer.parseInt(section); } catch (NumberFormatException ignored) {}

                        Lecture lecture = new Lecture();
                        lecture.setCourse(course);
                        lecture.setRoom(room);
                       // lecture.setTimeSlot(slot);
                        lecture.setSectionNumber(sectionNum);
                        lecture.setInstructor(instructor); // This is sufficient
                        newLectures.add(lecture); // Add to list for batch save

                    } catch (Exception e) {
                        warnings.add("Row " + (i + 1) + ": " + e.getMessage());
                        rowsSkipped++;
                    }
                }
            }
        }
        
        // --- Batch save all new entities after the loop ---
        courseRepository.saveAll(newCourses);
        instructorRepository.saveAll(newInstructors);
        roomRepository.saveAll(newRooms);
       // timeSlotRepository.saveAll(newTimeSlots);
        lectureRepository.saveAll(newLectures);

        return new ImportSummary(
                rowsProcessed, rowsSkipped,
                newCourses.size(),
                newRooms.size(),
                0,
                newLectures.size(),
                newInstructors.size(),
                warnings
        );
    }
}