package com.kaya.algorithm;

import com.kaya.model.Lecture;
import com.kaya.model.Room;
import com.kaya.model.TimeSlot;
import com.kaya.model.TimeTable;
import com.kaya.model.RoomType;
import com.kaya.model.TimeSlotType;

import java.util.*;

/**
 * Utility class containing the core genetic operators (Crossover and Mutation)
 * responsible for generating new offspring and introducing genetic diversity.
 */
public class GeneticOperators {

    // ------------------------------------------------------------------
    // 1. CROSSOVER
    // ------------------------------------------------------------------
    /**
     * Implements Uniform Crossover.
     * Instead of a single split point, each lecture is randomly inherited from
     * either Parent 1 or Parent 2 with a 50% probability. This maintains the
     * independence of classes and results in smarter, more robust offspring.
     *
     * @param p1 The first parent TimeTable.
     * @param p2 The second parent TimeTable.
     * @return A new offspring TimeTable combining genetic material from both parents.
     */
    public static TimeTable crossover(TimeTable p1, TimeTable p2) {

        Random rand = new Random();
        ArrayList<Lecture> childLectures = new ArrayList<>();

        for (int i = 0; i < p1.getLectures().size(); i++) {
            // Randomly select the gene (lecture) from either parent
            Lecture source = rand.nextBoolean() ? p1.getLectures().get(i) : p2.getLectures().get(i);

            // Deep Copy: Instantiate a new Lecture to ensure the offspring is completely independent.
            // This prevents accidental modifications to the parents' genetic material.
            childLectures.add(new Lecture(
                    source.getId(),
                    source.getCourse(),
                    source.getRoom(),
                    source.getTimeSlot(),
                    source.getSectionNumber(),
                    source.getInstructor()
            ));
        }
        return new TimeTable(childLectures);
    }

    // ------------------------------------------------------------------
    // 2. MUTATION
    // ------------------------------------------------------------------
    /**
     * Multi-Targeted & Smarter Mutation Strategy.
     * Dynamically alters a specific percentage of conflicting classes to rapidly resolve existing conflicts.
     * It intelligently decides whether to change the Room, the TimeSlot, or both simultaneously.
     *
     * @param tt                  The TimeTable (Chromosome) to be mutated.
     * @param timePools           The master map of valid time slots.
     * @param roomPools           The master map of valid rooms.
     * @param mutationImpactRatio The percentage of conflicting lectures to target during this mutation.
     */
    public static void mutate(TimeTable tt, Map<TimeSlotType, HashSet<TimeSlot>> timePools, Map<RoomType, HashSet<Room>> roomPools, double mutationImpactRatio) {

        Random rand = new Random();
        List<Lecture> lecturePool = new ArrayList<>(tt.getReport().getConflictingLectures());

        // Fallback Mechanism: If a mutation is triggered but no conflicts exist,
        // pick from the entire lecture list to forcefully introduce diversity.
        if (lecturePool.isEmpty()) {
            lecturePool = new ArrayList<>(tt.getLectures());
        }

        // Dynamic impact: mutate at least 1 lecture, or a calculated percentage of the conflicting pool
        int numMutations = Math.max(1, (int)(lecturePool.size() * mutationImpactRatio));

        // Randomize the pool to ensure we don't repeatedly mutate the same subset of classes
        Collections.shuffle(lecturePool);

        for (int i = 0; i < numMutations; i++) {
            Lecture targetLecture = lecturePool.get(i);

            // Fetch valid resources dynamically using the PoolHelper
            List<TimeSlot> finalTimePool = new ArrayList<>(PoolHelper.getValidTimeSlots(targetLecture, timePools));
            List<Room> finalRoomPool = new ArrayList<>(PoolHelper.getValidRooms(targetLecture, roomPools));

            // Determine mutation behavior (0: Room only, 1: Time only, 2: Both)
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