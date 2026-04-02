package org.example.timetable.algorithm;

import org.example.timetable.model.*;
import org.example.timetable.model.enums.CourseType;
import org.example.timetable.model.enums.RoomType;

import java.util.*;

/**
 * Clean Schedule Class
 * Represents a single chromosome (a complete proposed schedule).
 */
public class Schedule {
    private final List<CourseOffering> offerings;
    private int fitness = -1;
    private boolean isFitnessChanged = true; // Caching flag to prevent redundant fitness calculations

    public Schedule(List<CourseOffering> offerings) {
        this.offerings = offerings;
    }

    public List<CourseOffering> getOfferings() {
        return offerings;
    }

    /**
     * Randomly initializes the schedule.
     * Uses separated time pools (On-Site vs Online) based on course type.
     */
    public void initializeRandomly(Map<RoomType, List<Room>> roomPools, List<TimeSlot> onSiteTimePool, List<TimeSlot> onlineTimePool) {
        Random rand = new Random();
        for (CourseOffering offering : offerings) {

            // 1. Determine if the course is online to pick the correct time pool
            boolean isOnline = !offering.getCourse().getCourseType().requiresRoom();
            List<TimeSlot> validTimePool = isOnline ? onlineTimePool : onSiteTimePool;

            // Assign a random time slot safely
            if (validTimePool != null && !validTimePool.isEmpty()) {
                offering.setTimeSlot(validTimePool.get(rand.nextInt(validTimePool.size())));
            } else {
                offering.setTimeSlot(null); // No valid time slots, assign null
                System.err.println("Warning: No available time slots in timePool. Assigning null.");
            }

            // 2. Assign a random room only if the course requires a room
            if (!isOnline) {
                RoomType reqType = offering.getCourse().getRequiredRoomType();
                List<Room> validRooms = roomPools.get(reqType);

                // Ensure that validRooms is not empty before selecting a random room
                if (validRooms != null && !validRooms.isEmpty()) {
                    offering.setRoom(validRooms.get(rand.nextInt(validRooms.size())));
                } else {
                    offering.setRoom(null); // No valid rooms, assign null
                    System.err.println("Warning: No available rooms for type " + reqType + ". Assigning null.");
                }
            } else {
                offering.setRoom(null); // Online Course does not require a room
            }
        }
        isFitnessChanged = true; // Data changed, recalculate fitness
    }

    /**
     * Calculates the fitness score of the schedule.
     * Closer to 0 means fewer conflicts.
     */
    public int getFitness() {
        if (isFitnessChanged) {
            fitness = calculateFitness();
            isFitnessChanged = false;
        }
        return fitness;
    }

    private int calculateFitness() {
        int totalPenalty = 0;

        Map<Room, List<CourseOffering>> roomGroups = new HashMap<>();
        Map<Instructor, List<CourseOffering>> instructorGroups = new HashMap<>();

        for (CourseOffering offering : offerings) {
            // --- HARD CONSTRAINTS (Preventing Fitness Hack) ---
            if (offering.getTimeSlot() == null) {
                totalPenalty -= 100; // Big penalty for missing time
            }
            if (offering.getCourse().getCourseType().requiresRoom() && offering.getRoom() == null) {
                totalPenalty -= 100; // Big penalty for missing required room
            }

            // Group by room (ignoring online courses with no rooms)
            if (offering.getRoom() != null) {
                roomGroups.computeIfAbsent(offering.getRoom(), k -> new ArrayList<>()).add(offering);
            }
            // Group by instructor
            instructorGroups.computeIfAbsent(offering.getInstructor(), k -> new ArrayList<>()).add(offering);
        }

        // 1. Check for Room conflicts
        for (List<CourseOffering> roomList : roomGroups.values()) {
            totalPenalty += checkInternalConflicts(roomList);
        }

        // 2. Check for Instructor conflicts
        for (List<CourseOffering> instructorList : instructorGroups.values()) {
            totalPenalty += checkInternalConflicts(instructorList);
        }

        return totalPenalty;
    }

    /**
     * Helper method to find conflicts within a specific group.
     */
    private int checkInternalConflicts(List<CourseOffering> group) {
        int penalty = 0;
        for (int i = 0; i < group.size(); i++) {
            for (int j = i + 1; j < group.size(); j++) {
                CourseOffering c1 = group.get(i);
                CourseOffering c2 = group.get(j);

                // Ensure both have valid time slots before comparing
                if (c1.getTimeSlot() != null && c2.getTimeSlot() != null) {
                    if (c1.getTimeSlot().conflictsWith(c2.getTimeSlot())) {
                        penalty -= 10; // Deduct 10 points for each conflict
                    }
                }
            }
        }
        return penalty;
    }

    /**
     * Mutates a random offering to maintain genetic diversity.
     */
    public void mutate(Map<RoomType, List<Room>> roomPools, List<TimeSlot> onSiteTimePool, List<TimeSlot> onlineTimePool) {
        Random rand = new Random();
        CourseOffering randomOffering = this.offerings.get(rand.nextInt(this.offerings.size()));

        if (rand.nextBoolean()) {
            // 50% chance to mutate the time slot safely based on course type
            boolean isOnline = !randomOffering.getCourse().getCourseType().requiresRoom();
            List<TimeSlot> validTimePool = isOnline ? onlineTimePool : onSiteTimePool;

            if (validTimePool != null && !validTimePool.isEmpty()) {
                randomOffering.setTimeSlot(validTimePool.get(rand.nextInt(validTimePool.size())));
            }
        } else {
            // 50% chance to mutate the room safely
            if (randomOffering.getCourse().getCourseType().requiresRoom()) {
                RoomType reqType = randomOffering.getCourse().getRequiredRoomType();
                List<Room> validRooms = roomPools.get(reqType);
                if (validRooms != null && !validRooms.isEmpty()) {
                    randomOffering.setRoom(validRooms.get(rand.nextInt(validRooms.size())));
                }
            }
        }
        isFitnessChanged = true; // Mutation alters the schedule, recalculate fitness
    }

    /**
     * Combines two parent schedules to produce a child schedule (Crossover).
     */
    public static Schedule crossover(Schedule parent1, Schedule parent2) {
        Random rand = new Random();
        int splitPoint = rand.nextInt(parent1.getOfferings().size());

        List<CourseOffering> childOfferings = new ArrayList<>();

        for (int i = 0; i < parent1.getOfferings().size(); i++) {
            CourseOffering source = (i < splitPoint) ? parent1.getOfferings().get(i) : parent2.getOfferings().get(i);

            // Deep copy to ensure child is independent of parents
            CourseOffering newOffering = new CourseOffering(
                    source.getId(),
                    source.getCourse(),
                    source.getInstructor(),
                    source.getTimeSlot(),
                    source.getRoom()
            );
            childOfferings.add(newOffering);
        }
        return new Schedule(childOfferings);
    }
}