package org.example.timetable.data;

import org.dhatim.fastexcel.reader.ReadableWorkbook;
import org.dhatim.fastexcel.reader.Row;
import org.dhatim.fastexcel.reader.Sheet;
import org.example.timetable.model.*;
import org.example.timetable.model.enums.*;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Stream;

public class ExcelDataLoader {

    private final Set<Room> rooms = new HashSet<>();
    private final Set<Course> courses = new HashSet<>();
    private final Map<String, Instructor> instructors = new HashMap<>();
    private final List<CourseOffering> offerings = new ArrayList<>();

    public void loadData(String resourceFileName) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceFileName)) {

            if (is == null) {
                System.err.println("Error: Excel file not found in resources: " + resourceFileName);
                return;
            }

            try (ReadableWorkbook wb = new ReadableWorkbook(is)) {
                Sheet sheet = wb.getFirstSheet();
                int offeringIdCounter = 1;

                try (Stream<Row> rows = sheet.openStream()) {
                    Iterator<Row> rowIterator = rows.iterator();

                    if (rowIterator.hasNext()) {
                        rowIterator.next();
                    }

                    while (rowIterator.hasNext()) {
                        Row r = rowIterator.next();

                        if (r.getCellCount() < 18) continue;

                        Course course = extractCourse(r);
                        courses.add(course);

                        Instructor instructor = extractInstructor(r, course);

                        Room room = extractRoom(r, course.getRequiredRoomType());
                        if (room != null) {
                            rooms.add(room);
                        }

                        CourseOffering offering = new CourseOffering(offeringIdCounter++, course, instructor);
                        offering.setRoom(room);
                        offering.setTimeSlot(null);
                        offerings.add(offering);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading the Excel file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String getSafeText(Row r, int index) {
        if (index < r.getCellCount()) {
            String text = r.getCellText(index);
            return text != null ? text.trim() : "";
        }
        return "";
    }

    private Course extractCourse(Row r) {
        String courseSymbol = getSafeText(r, 0);
        String courseNumber = getSafeText(r, 1);
        String teachingMethod = getSafeText(r, 20);

        CourseType courseType = CourseType.ON_SITE;
        if (teachingMethod.contains("إلكتروني")) courseType = CourseType.ONLINE;
        else if (teachingMethod.contains("مدمج")) courseType = CourseType.BLENDED;

        RoomType requiredRoomType = courseNumber.contains("L") ? RoomType.LAB : RoomType.LECTURE;

        return new Course(courseSymbol, courseNumber, courseType, requiredRoomType, new ArrayList<>());
    }

    private Instructor extractInstructor(Row r, Course course) {
        String instructorName = getSafeText(r, 19);
        if (instructorName.isEmpty() || instructorName.equals("-")) {
            instructorName = "TBD";
        }

        Instructor instructor = instructors.computeIfAbsent(instructorName,
                name -> new Instructor(name, name, 15, new HashSet<>()));

        instructor.getQualifiedCourses().add(course);
        return instructor;
    }

    private Room extractRoom(Row r, RoomType type) {
        String roomString = getSafeText(r, 17);
        if (roomString.contains("Oline") || roomString.contains("ميدان") || roomString.isEmpty()) {
            return null;
        }

        String[] parts = roomString.split("\\s+");
        if (parts.length >= 2) {
            return new Room(parts[0], parts[1], type);
        }
        return new Room("Unknown", roomString, type);
    }

    public Set<Room> getRooms() { return rooms; }
    public Set<Course> getCourses() { return courses; }
    public List<Instructor> getInstructors() { return new ArrayList<>(instructors.values()); }
    public List<CourseOffering> getOfferings() { return offerings; }
}