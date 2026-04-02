package org.example.timetable.model;

import java.util.Objects;
import java.util.Set;

/**
 * Clean Instructor Class
 * يمثل المحاضر ويحتوي على قيوده الخاصة مثل الحد الأقصى للساعات والكورسات المؤهل لتدريسها.
 */
public class Instructor {
    private final String id;
    private final String name;
    private final int maxHours;
    private final Set<Course> qualifiedCourses;

    public Instructor(String id, String name, int maxHours, Set<Course> qualifiedCourses) {
        this.id = id;
        this.name = name;
        this.maxHours = maxHours;
        this.qualifiedCourses = qualifiedCourses;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public int getMaxHours() { return maxHours; }
    public Set<Course> getQualifiedCourses() { return qualifiedCourses; }

    /**
     * دالة مساعدة للـ Fitness لمعرفة هل الدكتور مؤهل لتدريس كورس معين أم لا
     */
    public boolean canTeach(Course course) {
        return qualifiedCourses.contains(course);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Instructor that = (Instructor) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return name + " (" + id + ")";
    }
}