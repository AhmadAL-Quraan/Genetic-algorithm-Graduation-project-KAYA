package com.kaya.algorithm;

import com.kaya.model.Lecture;
import com.kaya.model.Room;
import com.kaya.model.TimeSlot;
import com.kaya.model.TimeTable;
import com.kaya.model.enums.RoomType;
import com.kaya.model.enums.TeachingMethod;

import java.util.*;

public class GeneticOperators {

    public static TimeTable crossover(TimeTable p1, TimeTable p2) {
        Random rand = new Random();
        ArrayList<Lecture> childLectures = new ArrayList<>();

        for (int i = 0; i < p1.getLectures().size(); i++) {
            Lecture source = rand.nextBoolean() ? p1.getLectures().get(i) : p2.getLectures().get(i);
            Lecture child = new Lecture();
            child.setId(source.getId());
            child.setCourse(source.getCourse());
            child.setRoom(source.getRoom());
            child.setTimeSlot(source.getTimeSlot());
            child.setSectionNumber(source.getSectionNumber());
            child.setTeacher(source.getTeacher());
            child.setInstructor(source.getInstructor());
            childLectures.add(child);
        }
        return new TimeTable(childLectures);
    }

    public static void mutate(TimeTable tt,
                               Map<TeachingMethod, HashSet<TimeSlot>> timePools,
                               Map<RoomType, HashSet<Room>> roomPools,
                               double mutationImpactRatio) {
        Random rand = new Random();

        List<Lecture> lecturePool = new ArrayList<>(tt.getReport().getConflictingLectures());

        if (lecturePool.isEmpty()) {
            lecturePool = new ArrayList<>(tt.getLectures());
        }

        int numMutations = Math.max(1, (int) (lecturePool.size() * mutationImpactRatio));

        Collections.shuffle(lecturePool);

        for (int i = 0; i < numMutations && i < lecturePool.size(); i++) {
            Lecture targetLecture = lecturePool.get(i);

            List<TimeSlot> finalTimePool = new ArrayList<>(PoolHelper.getValidTimeSlots(targetLecture, timePools));
            List<Room> finalRoomPool = new ArrayList<>(PoolHelper.getValidRooms(targetLecture, roomPools));

            int mutationType = rand.nextInt(3);

            if (mutationType == 0 || mutationType == 2) {
                if (!finalRoomPool.isEmpty()) {
                    targetLecture.setRoom(finalRoomPool.get(rand.nextInt(finalRoomPool.size())));
                }
            }
            if (mutationType == 1 || mutationType == 2) {
                if (!finalTimePool.isEmpty()) {
                    targetLecture.setTimeSlot(finalTimePool.get(rand.nextInt(finalTimePool.size())));
                }
            }
        }
    }
}
