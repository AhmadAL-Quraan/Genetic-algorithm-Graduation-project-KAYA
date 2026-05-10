package com.kaya.repository;

import com.kaya.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseRepository extends JpaRepository<Course, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE course SET teacher_id = NULL WHERE teacher_id = :teacherId", nativeQuery = true)
    void detachTeacher(@Param("teacherId") Long teacherId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE course SET teacher_id = NULL WHERE teacher_id IS NOT NULL", nativeQuery = true)
    void detachAllTeachers();
}
