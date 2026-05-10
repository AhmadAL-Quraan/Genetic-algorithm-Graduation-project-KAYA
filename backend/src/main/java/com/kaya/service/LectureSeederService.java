package com.kaya.service;

import com.kaya.model.Course;
import com.kaya.model.Lecture;
import com.kaya.repository.CourseRepository;
import com.kaya.repository.LectureRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LectureSeederService {

    private final LectureRepository lectureRepository;
    private final CourseRepository courseRepository;

    @PostConstruct
    @Transactional
    public void seedIfEmpty() {
        long templateCount = lectureRepository.findByTimetableIdIsNull().size();
        if (templateCount > 0) {
            return;
        }

        List<Course> courses = courseRepository.findAll();
        if (courses.isEmpty()) {
            return;
        }

        List<Lecture> templates = new ArrayList<>();
        for (Course course : courses) {
            Lecture lecture = new Lecture();
            lecture.setCourse(course);
            templates.add(lecture);
        }

        lectureRepository.saveAll(templates);
        System.out.println("[LectureSeeder] Created " + templates.size()
                + " lecture templates from existing courses.");
    }
}
