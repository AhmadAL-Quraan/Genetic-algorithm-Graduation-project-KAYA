package com.kaya.algorithm;

import com.kaya.model.Lecture;
import com.kaya.model.Room;
import com.kaya.model.TimeSlot;
import com.kaya.model.TimeTable;

import java.util.*;

public class GeneticOperators {

    // ------------------------------------------------------------------
    // 1. CROSSOVER (التزاوج)
    // ------------------------------------------------------------------
    public static TimeTable crossover(TimeTable p1, TimeTable p2) {

        // =============== الكود القديم (Single-Point Crossover) ===============
        // Static Crossover: Creates a child from two parents
        Random rand = new Random();
        int split = rand.nextInt(p1.lectures.size());
        ArrayList<Lecture> childLectures = new ArrayList<>();

        for (int i = 0; i < p1.lectures.size(); i++) {
            Lecture source = (i < split) ? p1.lectures.get(i) : p2.lectures.get(i);
            // Create a NEW Class object but keep the same Course/Instructor
            // We take the Room and TimeSlot from the chosen parent
            childLectures.add(new Lecture(source.getId(),source.getCourse(),source.getRoom(), source.getTimeSlot(), source.getNumber(), source.getInstructor()));
        }
        return new TimeTable(childLectures);
        // ====================================================================


        /*
        // =============== الكود الجديد (Uniform Crossover) ===============
        // الفكرة هنا: لكل كورس، بنرمي عملة (50/50). يا ناخده من الأب الأول، يا من الأب التاني.
        // ده بيحافظ على استقلالية الكورسات وبيعمل تزاوج أذكى وأقوى في مشاكل الجداول.
        Random rand = new Random();
        ArrayList<Lecture> childLectures = new ArrayList<>();

        for (int i = 0; i < p1.lectures.size(); i++) {
            // الاختيار عشوائي لكل كورس على حدة
            Lecture source = rand.nextBoolean() ? p1.lectures.get(i) : p2.lectures.get(i);

            // إنشاء كورس جديد (Deep Copy) عشان ما نعدلش في الآباء بالغلط
            childLectures.add(new Lecture(source.getId(), source.getCourse(), source.getRoom(), source.getTimeSlot(), source.getNumber(), source.getInstructor()));
        }
        return new TimeTable(childLectures);
        */
    }


    // ------------------------------------------------------------------
    // 2. MUTATION (الطفرة)
    // ------------------------------------------------------------------
    public static void mutate(TimeTable tt, Map<String, HashSet<TimeSlot>> timePools, Map<String, HashSet<Room>> roomPools, double mutationImpactRatio) {

        // Targeted Reassignment Mutation
        // =============== الكود القديم (Targeted Single-Class Mutation) ===============
        List<TimeSlot> finalTimePool;
        List<Room> finalRoomPool;
        ArrayList<Lecture> lecturePool;

        Random rand = new Random();
        // Pick ONE random class to change
        lecturePool = new ArrayList<>(tt.report.getConflictingLectures());

        // خطوة أمان برمجية بسيطة: لو مفيش تعارضات، يعمل الطفرة على أي كورس عشوائي
        if(lecturePool.isEmpty()) {
            lecturePool = new ArrayList<>(tt.lectures);
        }

        Lecture randomLecture = lecturePool.get(rand.nextInt(lecturePool.size()));
        finalTimePool = new ArrayList<>(PoolHelper.getValidTimeSlots(randomLecture, timePools));
        finalRoomPool = new ArrayList<>(PoolHelper.getValidRooms(randomLecture, roomPools));

        if (rand.nextBoolean()) {
            // 50% chance to change the Room
            randomLecture.setRoom(finalRoomPool.get(rand.nextInt(finalRoomPool.size())));
        } else {
            // 50% chance to change the TimeSlot
            randomLecture.setTimeSlot(finalTimePool.get(rand.nextInt(finalTimePool.size())));
        }
        // =============================================================================


        /*
        // =============== الكود الجديد (Multi-Targeted & Smarter Mutation) ===============
        // الفكرة هنا: هنعدل أكتر من كورس متعارض في نفس الوقت (بناء على النسبة mutationImpactRatio)
        // وهنخلي الطفرة أحياناً تغير القاعة، أو الوقت، أو "الاتنين مع بعض"!
        Random rand = new Random();
        List<Lecture> lecturePool = new ArrayList<>(tt.report.getConflictingLectures());

        // لو الجدول سليم مفهوش تعارضات، نختار الطفرة من أي كورس عشوائي
        if(lecturePool.isEmpty()) {
            lecturePool = new ArrayList<>(tt.lectures);
        }

        // تحديد عدد الكورسات اللي هيتعملها طفرة (على الأقل كورس واحد، أو 20% من المتعارضين)
        int numMutations = Math.max(1, (int)(lecturePool.size() * mutationImpactRatio));

        // بنلخبط القائمة عشان نختار كورسات متعارضة عشوائية مختلفة
        Collections.shuffle(lecturePool);

        for (int i = 0; i < numMutations; i++) {
            Lecture targetLecture = lecturePool.get(i);
            List<TimeSlot> finalTimePool = new ArrayList<>(PoolHelper.getValidTimeSlots(targetLecture, timePools));
            List<Room> finalRoomPool = new ArrayList<>(PoolHelper.getValidRooms(targetLecture, roomPools));

            // اختيار نوع التغيير عشوائياً (0: قاعة فقط، 1: وقت فقط، 2: قاعة ووقت مع بعض)
            int mutationType = rand.nextInt(3);

            if (mutationType == 0 || mutationType == 2) {
                // تغيير القاعة
                if (!finalRoomPool.isEmpty()) {
                    targetLecture.setRoom(finalRoomPool.get(rand.nextInt(finalRoomPool.size())));
                }
            }
            if (mutationType == 1 || mutationType == 2) {
                // تغيير الوقت
                if (!finalTimePool.isEmpty()) {
                    targetLecture.setTimeSlot(finalTimePool.get(rand.nextInt(finalTimePool.size())));
                }
            }
        }
        */
    }
}