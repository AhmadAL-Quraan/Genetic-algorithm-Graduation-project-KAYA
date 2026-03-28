package solver;

import com.google.ortools.sat.BoolVar;
import com.google.ortools.sat.CpSolver;

import model.Course;
import model.Room;
import model.TimeSlot;

import java.util.List;

public class SolutionPrinter {

    public static void print(
            CpSolver solver,
            BoolVar[][] x,
            BoolVar[][] roomAssign,
            List<Course> courses,
            List<TimeSlot> slots,
            List<Room> rooms) {

        System.out.println(
                "===== GENERATED TIMETABLE ====="
        );

        for (int c = 0; c < courses.size(); c++) {

            Course course =
                    courses.get(c);

            for (int s = 0; s < slots.size(); s++) {

                if (solver.booleanValue(
                        x[c][s])) {

                    TimeSlot slot =
                            slots.get(s);

                    String roomName = "ONLINE";

                    if (!course.getIsOnline()) {

                        for (int r = 0;
                             r < rooms.size();
                             r++) {

                            if (solver.booleanValue(
                                    roomAssign[c][r])) {

                                roomName =
                                        rooms.get(r).getCode();
                            }
                        }
                    }

                    System.out.printf(
                            "Course: %-3s %-4s | Section: %-2s | Course Name: %-40s | Teacher Name: %-25s | Teacher: %-3s | Day: %-4s | Start: %-6s | Room: %-10s%n",
                            course.getDepartment(),
                            course.getCourseNumber(),
                            course.getSection(),
                            course.getCourseName(),
                            course.getTeacherName(),
                            course.getTeacherId(),
                            slot.getDayPattern(),
                            slot.convertMinutesToHour(slot.getStartMinutes()),
                            course.getRoomCode()
                    );

                    /*
                    System.out.println(

                            "Course: "
                                    + course.getDepartment()
                                    + " "
                                    + course.getCourseNumber()
                                    + " | Section Number: "
                                    + course.getSection()
                                    + " | Course Name: "
                                    + course.getCourseName()
                                    + " | Teacher Name: "
                                    + course.getTeacherName()
                                    + " | Teacher: "
                                    + course.getTeacherId()

                                    + " | Day: "
                                    + slot.getDayPattern()

                                    + " | Start: "
                                    + slot.convertMinutesToHour(slot.getStartMinutes())

                                    + " | Room: "
                                    + course.getRoomCode()

                    );
                    */
                }
            }
        }
    }
}