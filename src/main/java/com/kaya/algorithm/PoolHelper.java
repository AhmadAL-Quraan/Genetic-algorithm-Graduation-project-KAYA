package com.kaya.algorithm;

import com.kaya.model.Lecture;
import com.kaya.model.Room;
import com.kaya.model.TimeSlot;
import com.kaya.model.enums.RoomType;
import com.kaya.model.enums.TeachingMethod;

import java.util.HashSet;
import java.util.Map;

public class PoolHelper {

    // [تعديل جوهري]: الكود هنا صغر جداً وبقى أسرع بكتير!
    // زمان كنا بنجيب تقاطع (Intersection) لأن الكورس كان ممكن يكون ليه كذا نوع.
    // دلوقتي بما إننا استخدمنا Enum وبقى الكورس ليه نوع قاعة واحد وطريقة تدريس واحدة،
    // بنجيب اللستة المناسبة من الـ Map في خطوة واحدة بس O(1).

    /* // --- الكود القديم للتوضيح ---
    public static HashSet<TimeSlot> getValidTimeSlots(Lecture lecture, Map<String, HashSet<TimeSlot>> timePools) {
        HashSet<TimeSlot> timeSlotsSet = new HashSet<>(timePools.get(new ArrayList<>(lecture.getCourse().getTimeGroups()).get(0)));
        for (Map.Entry<String, HashSet<TimeSlot>> entry : timePools.entrySet()) {
            if (lecture.getCourse().getTimeGroups().contains(entry.getKey())) {
                timeSlotsSet.retainAll(entry.getValue());
            }
        }
        return timeSlotsSet;
    }
    */

    public static HashSet<TimeSlot> getValidTimeSlots(Lecture lecture, Map<TeachingMethod, HashSet<TimeSlot>> timePools) {
        // بنجيب طريقة التدريس بتاعة الكورس من الـ Enum، ونسحب الأوقات بتاعتها مباشرة
        TeachingMethod method = lecture.getCourse().getTeachingMethod();

        // بنعمل نسخة (new HashSet) عشان الخوارزمية متعدلش على الـ Pool الأساسي بتاع الجامعة بالغلط
        return new HashSet<>(timePools.get(method));
    }

    /* // --- الكود القديم للتوضيح ---
    public static HashSet<Room> getValidRooms(Lecture lecture, Map<String, HashSet<Room>> roomPools) {
        HashSet<Room> roomSlotsSet = new HashSet<>(roomPools.get(new ArrayList<>(lecture.getCourse().getRoomGroups()).get(0)));
        for(Map.Entry<String, HashSet<Room>> entry : roomPools.entrySet()) {
            if (lecture.getCourse().getRoomGroups().contains(entry.getKey())) {
                roomSlotsSet.retainAll(entry.getValue());
            }
        }
        return roomSlotsSet;
    }
    */

    public static HashSet<Room> getValidRooms(Lecture lecture, Map<RoomType, HashSet<Room>> roomPools) {
        // بنجيب نوع القاعة المطلوبة للكورس من الـ Enum، ونسحب القاعات المناسبة مباشرة
        RoomType type = lecture.getCourse().getRequiredRoomType();
        return new HashSet<>(roomPools.get(type));
    }
}