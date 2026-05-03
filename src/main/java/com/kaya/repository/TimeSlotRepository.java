package com.kaya.repository;

import com.kaya.model.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {}