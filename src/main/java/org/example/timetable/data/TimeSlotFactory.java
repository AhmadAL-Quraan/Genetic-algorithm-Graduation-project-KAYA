package org.example.timetable.data;

import org.example.timetable.model.TimeSlot;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Clean TimeSlot Factory
 * مسؤول عن توليد جميع الأوقات المتاحة في الجامعة برمجياً بناءً على القواعد الثابتة.
 */
public class TimeSlotFactory {

    public static List<TimeSlot> generateTimeSlots() {
        List<TimeSlot> slots = new ArrayList<>();

        // مجموعات الأيام
        Set<DayOfWeek> sttDays = Set.of(DayOfWeek.SUNDAY, DayOfWeek.TUESDAY, DayOfWeek.THURSDAY);
        Set<DayOfWeek> smwDays = Set.of(DayOfWeek.SATURDAY, DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY);

        // ==========================================
        // 1. نظام (أحد، ثلاثاء، خميس) - مدة المحاضرة 60 دقيقة
        // ==========================================
        // الوجاهي: من 8:00 صباحاً (480) إلى 3:00 مساءً (900)
        for (int t = 480; t < 900; t += 60) {
            slots.add(new TimeSlot(t, t + 60, sttDays, false));
        }
        // الأونلاين: من 4:00 مساءً (960) إلى 10:00 مساءً (1320)
        for (int t = 960; t < 1320; t += 60) {
            slots.add(new TimeSlot(t, t + 60, sttDays, true));
        }

        // ==========================================
        // 2. نظام (سبت، إثنين، أربعاء) - مدة المحاضرة 90 دقيقة للوجاهي و 60 للأونلاين
        // ==========================================
        // الوجاهي: من 8:00 صباحاً (480) إلى 3:30 مساءً (930) بزيادة 90 دقيقة
        for (int t = 480; t < 930; t += 90) {
            slots.add(new TimeSlot(t, t + 90, smwDays, false));
        }
        // الأونلاين: من 4:00 مساءً (960) إلى 10:00 مساءً (1320) بزيادة 60 دقيقة
        for (int t = 960; t < 1320; t += 60) {
            slots.add(new TimeSlot(t, t + 60, smwDays, true));
        }

        return slots;
    }
}