package org.example.timetable.data;

import org.example.timetable.model.*;
import org.example.timetable.model.enums.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Clean CSV Data Loader
 * Responsible for reading Data from CSV and creating clean Objects.
 * Time extraction has been removed as TimeSlots are now generated via TimeSlotFactory.
 */
public class CsvDataLoader {

    private final Set<Room> rooms = new HashSet<>();
    private final Set<Course> courses = new HashSet<>();
    private final Map<String, Instructor> instructors = new HashMap<>();
    private final List<CourseOffering> offerings = new ArrayList<>();

    public void loadData(String resourceFileName) {
        String cvsSplitBy = ",";

        // Professional way to load file from resources directory
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceFileName)) {

            if (is == null) {
                System.err.println("Error: Could not find the file in resources: " + resourceFileName);
                return;
            }

            // Using UTF-8 to prevent Arabic encoding issues
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                br.readLine(); // Skip Header

                int offeringIdCounter = 1;
                String line;

                while ((line = br.readLine()) != null) {
                    // -1 ensures empty trailing columns are not discarded by Java
                    String[] data = line.split(cvsSplitBy, -1);
                    if (data.length < 18) continue;

                    Course course = extractCourse(data);
                    courses.add(course);

                    Instructor instructor = extractInstructor(data, course);

                    Room room = extractRoom(data, course.getRequiredRoomType());
                    if (room != null) {
                        rooms.add(room);
                    }

                    // Create offering without a time slot (GA will handle time assignment)
                    CourseOffering offering = new CourseOffering(offeringIdCounter++, course, instructor);
                    offering.setRoom(room);
                    offering.setTimeSlot(null);
                    offerings.add(offering);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading the CSV file: " + e.getMessage());
        }
    }

    // --- Helper Methods ---

    /**
     * Safe getter to prevent IndexOutOfBounds errors if columns are missing.
     */
    private String getSafe(String[] data, int index) {
        return (index < data.length) ? data[index].trim() : "";
    }

    private Course extractCourse(String[] data) {
        String courseSymbol = getSafe(data, 0);
        String courseNumber = getSafe(data, 1);
        String teachingMethod = getSafe(data, 20);

        CourseType courseType = CourseType.ON_SITE;
        if (teachingMethod.contains("إلكتروني")) courseType = CourseType.ONLINE;
        else if (teachingMethod.contains("مدمج")) courseType = CourseType.BLENDED;

        RoomType requiredRoomType = courseNumber.contains("L") ? RoomType.LAB : RoomType.LECTURE;

        return new Course(courseSymbol, courseNumber, courseType, requiredRoomType, new ArrayList<>());
    }

    private Instructor extractInstructor(String[] data, Course course) {
        String instructorName = getSafe(data, 19);
        if (instructorName.isEmpty() || instructorName.equals("-")) {
            instructorName = "TBD";
        }

        Instructor instructor = instructors.computeIfAbsent(instructorName,
                name -> new Instructor(name, name, 15, new HashSet<>()));

        instructor.getQualifiedCourses().add(course);
        return instructor;
    }

    private Room extractRoom(String[] data, RoomType type) {
        String roomString = getSafe(data, 17);
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