package com.kaya.repository;

import com.kaya.model.TimeSlotType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TimeSlotTypeRepository extends JpaRepository<TimeSlotType, Long> {
}