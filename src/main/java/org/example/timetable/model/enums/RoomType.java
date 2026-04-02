package org.example.timetable.model.enums;

/**
 * Enum
 * يحدد نوع القاعة (مختبر أم قاعة تدريس عادية)
 * لضمان عدم وضع كورس عملي في قاعة نظرية والعكس
 */
public enum RoomType {
    LECTURE,
    LAB,
    UNSPECIFIED // للحالات التي لا تهمنا فيها نوع القاعة
}