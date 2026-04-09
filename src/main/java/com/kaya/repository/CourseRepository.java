package com.kaya.repository;

import com.kaya.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findBySymbolAndNumber(String symbol, String number);
    boolean existsBySymbolAndNumber(String symbol, String number);
}