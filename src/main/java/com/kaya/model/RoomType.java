package com.kaya.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class RoomType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String typeName; // مثلاً: "مختبر"، "قاعة عادية"

    public RoomType(String typeName) {
        this.typeName = typeName;
    }

    // مهم جداً للخوارزمية عشان تعرف تقارن الأنواع في الـ Maps
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoomType roomType = (RoomType) o;
        return Objects.equals(typeName, roomType.typeName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(typeName);
    }

    @Override
    public String toString() {
        return typeName;
    }
}