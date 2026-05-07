package com.kaya.repository;

import com.kaya.model.FitnessReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FitnessReportRepository extends JpaRepository<FitnessReport, Long> {}