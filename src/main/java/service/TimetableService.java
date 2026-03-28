package service;

import data.CSVReader;
import data.TimeSlotGenerator;
import model.*;
import solver.TimetableSolver;

import java.util.*;

public class TimetableService {

    public void run(String filePath) {

        // 1️⃣ Read Courses

        List<Course> courses =
                CSVReader.readCSV(
                        filePath
                );

        System.out.println(
                "Total Courses = "
                        + courses.size()
        );

        // 2️⃣ Build Teachers

        List<Teacher> teachers =
                buildTeachers(courses);

        // 3️⃣ Build Rooms

        List<Room> rooms =
                buildRooms(courses);

        // 4️⃣ Assign IDs

        assignTeacherIds(
                courses,
                teachers
        );

        assignRoomIds(
                courses,
                rooms
        );

        // 5️⃣ Patterns

        List<DayPattern> patterns =
                createPatterns();

        // 6️⃣ Slots

        List<TimeSlot> slots =
                TimeSlotGenerator.generate(
                        8,
                        15,
                        patterns
                );

        System.out.println(
                "Total Slots = "
                        + slots.size()
        );

//        System.out.println("Slots: ");
//        for(TimeSlot s: slots)
//        {
//            System.out.println(s);
//        }

        // 7️⃣ Test Assignment

        assignTestPatterns(
                courses,
                patterns,
                slots
        );

        // 8️⃣ Run Solver

        TimetableSolver solver =
                new TimetableSolver(
                        courses,
                        slots,
                        teachers,
                        rooms
                );

        solver.solve();
    }

    // =========================

    private List<Teacher> buildTeachers(
            List<Course> courses) {

        HashMap<String, Teacher> map =
                new HashMap<>();

        int id = 0;

        for (Course c : courses) {

            if (!map.containsKey(
                    c.getTeacherName())) {

                map.put(
                        c.getTeacherName(),
                        new Teacher(
                                id++,
                                c.getTeacherName()
                        )
                );
            }
        }

        System.out.println(
                "Total Teachers = "
                        + map.size()
        );

        return new ArrayList<>(
                map.values()
        );
    }

    // =========================

    private List<Room> buildRooms(
            List<Course> courses) {

        HashMap<String, Room> map =
                new HashMap<>();

        int id = 0;

        for (Course c : courses) {

            if (c.getIsOnline())
                continue;

            if (!map.containsKey(
                    c.getRoomCode())) {

                boolean isLab =
                        c.getRoomCode()
                                .toUpperCase()
                                .contains("L");

                map.put(
                        c.getRoomCode(),
                        new Room(
                                id++,
                                c.getRoomCode(),
                                isLab
                        )
                );
            }
        }

        System.out.println(
                "Total Rooms = "
                        + map.size()
        );

        return new ArrayList<>(
                map.values()
        );
    }

    // =========================

    private void assignTeacherIds(
            List<Course> courses,
            List<Teacher> teachers) {

        HashMap<String, Teacher> map =
                new HashMap<>();

        for (Teacher t : teachers) {

            map.put(
                    t.getName(),
                    t
            );
        }

        for (Course c : courses) {

            Teacher t =
                    map.get(
                            c.getTeacherName()
                    );

            if (t != null) {

                c.setTeacherId(t.getId());
            }
        }
    }

    // =========================

    private void assignRoomIds(
            List<Course> courses,
            List<Room> rooms) {

        HashMap<String, Room> map =
                new HashMap<>();

        for (Room r : rooms) {

            map.put(
                    r.getCode(),
                    r
            );
        }

        for (Course c : courses) {

            if (c.getIsOnline()) {

                c.setRoomId(-1);
                continue;
            }

            Room r =
                    map.get(
                            c.getRoomCode()
                    );

            if (r != null) {

                c.setRoomId(r.getId());
            }
        }
    }

    // =========================

    private List<DayPattern>
    createPatterns() {

        List<DayPattern> list =
                new ArrayList<>();

        list.add(
                new DayPattern(
                        0,
                        Arrays.asList(
                                "Sun",
                                "Tue",
                                "Thu"
                        ),
                        60
                )
        );

        list.add(
                new DayPattern(
                        1,
                        Arrays.asList(
                                "Sat",
                                "Mon",
                                "Wed"
                        ),
                        90
                )
        );

        return list;
    }

    // =========================

    private void assignTestPatterns(
            List<Course> courses,
            List<DayPattern> patterns,
            List<TimeSlot> slots) {

        for (int i = 0;
             i < courses.size();
             i++) {

            Course c =
                    courses.get(i);

            c.setPatternId(i % patterns.size());
        }

        for (int i = 0;
             i < courses.size();
             i++) {

            Course c =
                    courses.get(i);

            List<TimeSlot> valid =
                    new ArrayList<>();

            for (TimeSlot s : slots) {

                if (s.getPatternId()
                        == c.getPatternId()) {

                    valid.add(s);
                }
            }

            if (!valid.isEmpty()) {

                c.setTimeSlotId(valid.get(
                                i % valid.size()
                        ).getId());
            }
        }
    }

}