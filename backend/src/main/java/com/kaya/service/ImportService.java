package com.kaya.service;

import com.kaya.algorithm.data.ExcelDataExtractor;
import com.kaya.model.Course;
import com.kaya.model.Lecture;
import com.kaya.model.Room;
import com.kaya.model.Teacher;
import com.kaya.model.TimeSlot;
import com.kaya.model.enums.RoomType;
import com.kaya.repository.CourseRepository;
import com.kaya.repository.LectureRepository;
import com.kaya.repository.RoomRepository;
import com.kaya.repository.TeacherRepository;
import com.kaya.repository.TimeSlotRepository;
import org.dhatim.fastexcel.reader.ReadableWorkbook;
import org.dhatim.fastexcel.reader.Row;
import org.dhatim.fastexcel.reader.Sheet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Stream;

@Service
public class ImportService {

    private final CourseRepository courseRepo;
    private final RoomRepository roomRepo;
    private final TimeSlotRepository timeSlotRepo;
    private final LectureRepository lectureRepo;
    private final TeacherRepository teacherRepo;

    public ImportService(CourseRepository courseRepo, RoomRepository roomRepo,
                         TimeSlotRepository timeSlotRepo, LectureRepository lectureRepo,
                         TeacherRepository teacherRepo) {
        this.courseRepo = courseRepo;
        this.roomRepo = roomRepo;
        this.timeSlotRepo = timeSlotRepo;
        this.lectureRepo = lectureRepo;
        this.teacherRepo = teacherRepo;
    }

    public static class ImportSummary {
        public int rowsProcessed;
        public int rowsSkipped;
        public int coursesCreated;
        public int roomsCreated;
        public int timeSlotsCreated;
        public int lecturesCreated;
        public int teachersCreated;
        public List<String> warnings = new ArrayList<>();
    }

    @Transactional
    public ImportSummary importExcel(MultipartFile file) throws IOException {
        ImportSummary summary = new ImportSummary();

        // In-memory caches keyed by natural identity, seeded from existing DB rows.
        Map<String, Course> courseCache = new HashMap<>();
        for (Course c : courseRepo.findAll()) courseCache.put(courseKey(c.getCourseSymbol(), c.getCourseNumber()), c);

        Map<String, Room> roomCache = new HashMap<>();
        for (Room r : roomRepo.findAll()) roomCache.put(roomKey(r.getBuilding(), r.getRoomNumber()), r);

        Map<String, TimeSlot> timeSlotCache = new HashMap<>();
        for (TimeSlot t : timeSlotRepo.findAll()) timeSlotCache.put(timeSlotKey(t), t);

        // Teacher (Doctor) cache — deduplicate by trimmed name
        Map<String, Teacher> teacherCache = new HashMap<>();
        for (Teacher t : teacherRepo.findAll()) teacherCache.put(safe(t.getName()).trim().toLowerCase(), t);

        try (InputStream in = file.getInputStream();
             ReadableWorkbook wb = new ReadableWorkbook(in)) {

            Sheet sheet = wb.getFirstSheet();
            Map<String, Integer> headerMap = new HashMap<>();

            try (Stream<Row> headerStream = sheet.openStream()) {
                Optional<Row> headerRow = headerStream.findFirst();
                if (headerRow.isEmpty()) {
                    summary.warnings.add("Spreadsheet appears to be empty.");
                    return summary;
                }
                Row header = headerRow.get();
                for (int i = 0; i < header.getCellCount(); i++) {
                    String text = safe(header.getCellText(i)).trim();
                    if (text.contains("رمز المساق")) headerMap.put("COURSE_SYMBOL", i);
                    else if (text.contains("رقم المساق")) headerMap.put("COURSE_NUMBER", i);
                    else if (text.contains("الشعبة") && !text.contains("ملغاة")) headerMap.put("CLASS_NUMBER", i);
                    else if (text.contains("وقت البداية")) headerMap.put("START_TIME", i);
                    else if (text.contains("وقت النهاية")) headerMap.put("END_TIME", i);
                    else if (text.contains("رمز القاعة")) headerMap.put("ROOM_CODE", i);
                    else if (text.contains("المحاضر")) headerMap.put("INSTRUCTOR", i);
                    else if (text.contains("طريقة") && text.contains("تدريس")) headerMap.put("TEACHING_METHOD", i);
                    else if (text.equals("سبت")) headerMap.put("DAY_SAT", i);
                    else if (text.equals("حد")) headerMap.put("DAY_SUN", i);
                    else if (text.equals("ثن")) headerMap.put("DAY_MON", i);
                    else if (text.equals("ثل")) headerMap.put("DAY_TUE", i);
                    else if (text.equals("ربع")) headerMap.put("DAY_WED", i);
                    else if (text.equals("خمس")) headerMap.put("DAY_THU", i);
                }
            }

            for (String required : List.of("COURSE_SYMBOL", "COURSE_NUMBER", "ROOM_CODE", "START_TIME", "END_TIME", "INSTRUCTOR", "TEACHING_METHOD")) {
                if (!headerMap.containsKey(required)) {
                    summary.warnings.add("Missing required column: " + required);
                }
            }
            if (!summary.warnings.isEmpty()) return summary;

            try (Stream<Row> rows = sheet.openStream()) {
                rows.skip(1).forEach(r -> {
                    summary.rowsProcessed++;
                    try {
                        if (r.getCellCount() == 0 ||
                            safe(r.getCellText(headerMap.get("COURSE_SYMBOL"))).isEmpty()) {
                            summary.rowsSkipped++;
                            return;
                        }

                        Course parsedCourse = ExcelDataExtractor.extractCourse(r, headerMap);
                        Room parsedRoom = ExcelDataExtractor.extractRoom(r, headerMap);
                        TimeSlot parsedSlot = ExcelDataExtractor.extractTimeSlot(r, headerMap);
                        String classNumberStr = safe(r.getCellText(headerMap.get("CLASS_NUMBER")));
                        String instructorName = safe(r.getCellText(headerMap.get("INSTRUCTOR"))).trim();

                        if (parsedRoom == null
                            || parsedRoom.getRoomNumber() == null
                            || parsedRoom.getRoomNumber().contains("Oline")
                            || parsedRoom.getRoomNumber().contains("ميدان")) {
                            summary.rowsSkipped++;
                            return;
                        }
                        if (parsedSlot.getStartTime() == null || parsedSlot.getEndTime() == null) {
                            summary.rowsSkipped++;
                            return;
                        }

                        // Upsert Teacher (Doctor) by name — create once, reuse across rows
                        Teacher teacher = null;
                        if (!instructorName.isEmpty()) {
                            String nameKey = instructorName.toLowerCase();
                            teacher = teacherCache.computeIfAbsent(nameKey, k -> {
                                Teacher t = new Teacher();
                                t.setName(instructorName);
                                summary.teachersCreated++;
                                return teacherRepo.save(t);
                            });
                        }

                        Course course = courseCache.computeIfAbsent(
                            courseKey(parsedCourse.getCourseSymbol(), parsedCourse.getCourseNumber()),
                            k -> { summary.coursesCreated++; return courseRepo.save(parsedCourse); }
                        );

                        if (parsedCourse.getRequiredRoomType() == RoomType.LAB) parsedRoom.setRoomType(RoomType.LAB);
                        Room room = roomCache.computeIfAbsent(
                            roomKey(parsedRoom.getBuilding(), parsedRoom.getRoomNumber()),
                            k -> { summary.roomsCreated++; return roomRepo.save(parsedRoom); }
                        );

                        TimeSlot slot = timeSlotCache.computeIfAbsent(
                            timeSlotKey(parsedSlot),
                            k -> { summary.timeSlotsCreated++; return timeSlotRepo.save(parsedSlot); }
                        );

                        Lecture lecture = new Lecture();
                        lecture.setCourse(course);
                        lecture.setRoom(room);
                        lecture.setTimeSlot(slot);
                        lecture.setTeacher(teacher);
                        lecture.setInstructor(instructorName.isEmpty() ? null : instructorName);
                        try { lecture.setSectionNumber(classNumberStr.isEmpty() ? 1L : Long.parseLong(classNumberStr.trim())); }
                        catch (NumberFormatException ex) { lecture.setSectionNumber(1L); }
                        lectureRepo.save(lecture);
                        summary.lecturesCreated++;
                    } catch (Exception ex) {
                        summary.rowsSkipped++;
                        summary.warnings.add("Row error: " + ex.getMessage());
                    }
                });
            }
        }

        return summary;
    }

    private static String safe(String s) { return s == null ? "" : s; }
    private static String courseKey(String symbol, String number) { return safe(symbol).trim() + "|" + safe(number).trim(); }
    private static String roomKey(String building, String number) { return safe(building).trim() + "|" + safe(number).trim(); }
    private static String timeSlotKey(TimeSlot t) {
        String days = t.getDays() == null ? "" : t.getDays().stream().map(Enum::name).sorted().reduce((a, b) -> a + "," + b).orElse("");
        return t.getStartTime() + "|" + t.getEndTime() + "|" + days + "|" + t.getTeachingMethod();
    }
}
