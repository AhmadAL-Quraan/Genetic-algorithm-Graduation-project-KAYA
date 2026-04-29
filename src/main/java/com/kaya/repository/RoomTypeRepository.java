package com.kaya.repository;

import com.kaya.model.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomTypeRepository extends JpaRepository<RoomType, Long> {
    // Spring Boot هيعمل كل دوال الحفظ والبحث تلقائياً هنا
}