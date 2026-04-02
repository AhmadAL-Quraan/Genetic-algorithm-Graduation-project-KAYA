package org.example.timetable.model;

import org.example.timetable.model.enums.CourseType;
import org.example.timetable.model.enums.RoomType;

import java.util.List;
import java.util.Objects;

/**
 * Clean Course Class
 * يمثل المادة الدراسية فقط وخصائصها.
 */
public class Course {
    private final String symbol;
    private final String number;
    private final CourseType courseType;       // وجاهي، مدمج، أونلاين
    private final RoomType requiredRoomType;   // هل يحتاج لاب أم قاعة عادية؟
    private final List<String> studentGroups;  // بديل الـ majors لتمثيل مجموعات الطلاب (سنة أولى، ثانية.. الخ)

    public Course(String symbol, String number, CourseType courseType, RoomType requiredRoomType, List<String> studentGroups) {
        this.symbol = symbol;
        this.number = number;
        this.courseType = courseType;
        this.requiredRoomType = requiredRoomType;
        this.studentGroups = studentGroups;
    }

    // Getters
    public String getSymbol() { return symbol; }
    public String getNumber() { return number; }
    public CourseType getCourseType() { return courseType; }
    public RoomType getRequiredRoomType() { return requiredRoomType; }
    public List<String> getStudentGroups() { return studentGroups; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Course course = (Course) o;
        return Objects.equals(symbol, course.symbol) &&
                Objects.equals(number, course.number);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol, number);
    }

    @Override
    public String toString() {
        return symbol + " " + number;
    }
}