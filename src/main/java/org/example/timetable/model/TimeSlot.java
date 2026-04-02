package org.example.timetable.model;

import java.time.DayOfWeek;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * Clean TimeSlot Class (Optimized with Minutes)
 * Represents a specific time period using minutes from midnight for maximum GA performance.
 */
public class TimeSlot {
    private final int startTimeMinutes; // e.g., 480 = 8:00 AM
    private final int endTimeMinutes;
    private final Set<DayOfWeek> days;
    private final boolean isOnline; // Differentiates between morning (on-site) and evening (online) slots

    public TimeSlot(int startTimeMinutes, int endTimeMinutes, Set<DayOfWeek> days, boolean isOnline) {
        this.startTimeMinutes = startTimeMinutes;
        this.endTimeMinutes = endTimeMinutes;
        this.days = days;
        this.isOnline = isOnline;
    }

    public int getStartTimeMinutes() { return startTimeMinutes; }
    public int getEndTimeMinutes() { return endTimeMinutes; }
    public Set<DayOfWeek> getDays() { return days; }
    public boolean isOnline() { return isOnline; }

    /**
     * Extremely fast conflict detection using integer comparison.
     * Crucial for GA performance.
     */
    public boolean conflictsWith(TimeSlot other) {
        // If they share at least one day
        if (!Collections.disjoint(this.days, other.days)) {
            // Check for time overlap
            return this.startTimeMinutes < other.endTimeMinutes &&
                    other.startTimeMinutes < this.endTimeMinutes;
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeSlot timeSlot = (TimeSlot) o;
        return startTimeMinutes == timeSlot.startTimeMinutes &&
                endTimeMinutes == timeSlot.endTimeMinutes &&
                isOnline == timeSlot.isOnline &&
                Objects.equals(days, timeSlot.days);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startTimeMinutes, endTimeMinutes, days, isOnline);
    }

    @Override
    public String toString() {
        // Convert minutes back to HH:MM format for readability
        String start = String.format("%02d:%02d", startTimeMinutes / 60, startTimeMinutes % 60);
        String end = String.format("%02d:%02d", endTimeMinutes / 60, endTimeMinutes % 60);
        return "Days: " + days + " [" + start + " - " + end + "] " + (isOnline ? "(Online)" : "(On-Site)");
    }
}