package org.example.model;

import org.dhatim.fastexcel.reader.Row;

import java.time.LocalTime;
import java.util.*;

class TimeSlot {
    public LocalTime start_time;
    public LocalTime end_time;
    public Set<String> days;
    public ArrayList<String> groups;

    TimeSlot(LocalTime start_time, LocalTime end_time, Set<String> days, ArrayList<String> groups) {
        this.start_time = start_time;
        this.end_time = end_time;
        this.days = days;
        this.groups = groups;
    }

    public static TimeSlot extractTimeSlot(Row r, int startTimeIndex, int endTimeIndex) {
        Set<String> days = new HashSet<>();
        LocalTime startTime = null;
        LocalTime endTime = null;
        ArrayList<String> timeGroup = new ArrayList<String>();

        if (r.getCellText(6).equals("X")) days.add("Saturday");
        if (r.getCellText(7).equals("X")) days.add("Sunday");
        if (r.getCellText(8).equals("X")) days.add("Monday");
        if (r.getCellText(9).equals("X")) days.add("Tuesday");
        if (r.getCellText(10).equals("X")) days.add("Wednesday");
        if (r.getCellText(11).equals("X")) days.add("Thursday");
        if (r.getCellText(12).equals("X")) days.add("Friday");

        Double timeFractionStart = r.getCellAsNumber(startTimeIndex).get().doubleValue();
        Double timeFractionEnd = r.getCellAsNumber(endTimeIndex).get().doubleValue();

        if (timeFractionStart != null && timeFractionEnd != null) {
            // Convert fraction of day to total seconds
            // 86,400 = total seconds in a day (24 * 60 * 60)
            long totalSecondsStart = Math.round(timeFractionStart * 86400);
            long totalSecondsEnd = Math.round(timeFractionEnd * 86400);
            endTime = LocalTime.ofSecondOfDay(totalSecondsEnd);
            startTime = LocalTime.ofSecondOfDay(totalSecondsStart);
        }
        else {
            System.out.println("error parsing time");
            System.exit(1);
        }
        timeGroup.add(r.getCellText(20));
        return new TimeSlot(startTime, endTime, days, timeGroup);
    }
    public boolean conflictsWith(TimeSlot other) {
        // Collections.disjoint returns true if they have NOTHING in common.
        // So we negate it (!) to see if they share at least one day.
        if (!Collections.disjoint(this.days, other.days)) {
            return this.start_time.isBefore(other.end_time) &&
                    other.start_time.isBefore(this.end_time);
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeSlot timeSlot = (TimeSlot) o;
        return Objects.equals(start_time, timeSlot.start_time) &&
                Objects.equals(end_time, timeSlot.end_time) &&
                Objects.equals(days, timeSlot.days) &&
                Objects.equals(groups, timeSlot.groups);
        // Set equality ignores order!
    }
    // to convert an array of TimeSlots to a HashSet.
    @Override
    public int hashCode() {

        return Objects.hash(start_time, end_time, days, groups);
    }

    @Override
    public String toString() {
        return "Days : " + days + " Start : " + start_time + " End : " + end_time + " Group : " + groups;
    }
}