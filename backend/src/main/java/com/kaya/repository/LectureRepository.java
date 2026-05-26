package com.kaya.repository;

import com.kaya.model.Lecture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LectureRepository extends JpaRepository<Lecture, Long> {
    List<Lecture> findByTimetableIdIsNull();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Lecture l SET l.timeSlot = null WHERE l.timeSlot.id = :slotId")
    void detachTimeSlot(@Param("slotId") Long slotId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Lecture l SET l.course = null WHERE l.course.id = :courseId")
    void detachCourse(@Param("courseId") Long courseId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Lecture l SET l.course = null WHERE l.course IS NOT NULL")
    void detachAllCourses();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Lecture l SET l.instructor = null WHERE l.instructor.id = :instructorId")
    void detachInstructor(@Param("instructorId") Long instructorId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Lecture l SET l.instructor = null WHERE l.instructor IS NOT NULL")
    void detachAllInstructors();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Lecture l SET l.room = null WHERE l.room.id = :roomId")
    void detachRoom(@Param("roomId") Long roomId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Lecture l SET l.room = null WHERE l.room IS NOT NULL")
    void detachAllRooms();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Lecture l WHERE l.timetableId IS NULL AND l.course IS NULL")
    void deleteOrphanedTemplateLectures();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "DELETE FROM fitness_report_conflicting_lectures WHERE conflicting_lectures_id = :lectureId", nativeQuery = true)
    void clearConflictingLectureRef(@Param("lectureId") Long lectureId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "DELETE FROM fitness_report_conflicting_lectures", nativeQuery = true)
    void clearAllConflictingLectureRefs();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Lecture l WHERE l.timetableId IS NULL")
    void deleteAllTemplateLectures();
}
