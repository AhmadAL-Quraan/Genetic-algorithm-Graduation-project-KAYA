package com.kaya.algorithm;

import com.kaya.model.Lecture;
import com.kaya.model.Room;
import com.kaya.model.TimeSlot;
import com.kaya.model.TimeTable;
import com.kaya.model.enums.RoomType;
import com.kaya.model.enums.TeachingMethod;

import java.util.*;

import static com.kaya.algorithm.PoolHelper.getValidRooms;
import static com.kaya.algorithm.PoolHelper.getValidTimeSlots;

public class TimeTableInitializer {

    // نقلنا الميثود هنا وبقت بتاخد الـ TimeTable كـ Parameter عشان تعدل عليه
    // [تم التعديل]: استخدمنا الـ Enums في الـ Maps عشان تتوافق مع التعديلات الجديدة في الـ Models
    public static void initializeRandomly(TimeTable tt,
                                          Map<TeachingMethod, HashSet<TimeSlot>> timePools,
                                          Map<RoomType, HashSet<Room>> roomPools) {
        List<TimeSlot> timePool;
        List<Room> roomPool;

        Random rand = new Random();
        for (Lecture c : tt.getLectures()) {
            // بنسحب القاعات والأوقات المناسبة للكورس ده باستخدام الـ PoolHelper الأسرع من البرق
            timePool = new ArrayList<>(getValidTimeSlots(c, timePools));
            roomPool = new ArrayList<>(getValidRooms(c, roomPools));

            // Randomly pick an allele (or a gene) from the pools
            // خطوة أمان: بنتأكد إن الـ Pool مش فاضي عشان ميرميش IndexOutOfBoundsException
            if (!timePool.isEmpty()) {
                c.setTimeSlot(timePool.get(rand.nextInt(timePool.size())));
            }
            if (!roomPool.isEmpty()) {
                c.setRoom(roomPool.get(rand.nextInt(roomPool.size())));
            }
        }

        // حساب التقييم بعد التهيئة العشوائية
        FitnessCalculator.calculateFitness(tt);
    }
}