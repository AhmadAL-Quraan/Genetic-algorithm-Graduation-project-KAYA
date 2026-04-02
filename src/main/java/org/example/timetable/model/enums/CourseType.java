package org.example.timetable.model.enums;

/**
 * Smart Enum
 * يحدد نوع الكورس وهل يحتاج إلى قاعة أم لا.
 * هذا سيجعل دالة الـ Fitness سريعة جداً (O(1)) بدلاً من عمل حسابات معقدة.
 */
public enum CourseType {
    ON_SITE(true),   // وجاهي - يحتاج قاعة
    BLENDED(true),   // مدمج - يحتاج قاعة
    ONLINE(false);   // أونلاين - لا يحتاج قاعة

    private final boolean requiresRoom;

    CourseType(boolean requiresRoom) {
        this.requiresRoom = requiresRoom;
    }

    // دالة سريعة جداً لمعرفة هل نحتاج للبحث عن قاعة لهذا الكورس أم لا
    public boolean requiresRoom() {
        return requiresRoom;
    }
}