package com.kaya.repository;

import com.kaya.model.Lecture;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LectureRepository extends JpaRepository<Lecture, Long> {
    List<Lecture> findAllByCourse_Id(Long courseId);

    /** Returns only the original template lectures — those not owned by any timetable. */
    @org.springframework.data.jpa.repository.Query("SELECT l FROM Lecture l WHERE l.id NOT IN (SELECT l2.id FROM TimeTable tt JOIN tt.lectures l2)")
    List<Lecture> findAllTemplateLectures();
}