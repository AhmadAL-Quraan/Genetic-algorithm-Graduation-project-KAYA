package com.kaya.repository;

import com.kaya.model.Instructor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InstructorRepository extends JpaRepository<Instructor, Long> {
    Optional<Instructor> findByInstructorName(String instructorName);
}