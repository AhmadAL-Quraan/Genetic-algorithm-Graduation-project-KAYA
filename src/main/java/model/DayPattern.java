package model;

import java.util.List;

public class DayPattern {

    public int id;

    public List<String> days;

    public int durationMinutes;

    public DayPattern(
            int id,
            List<String> days,
            int durationMinutes) {

        this.id = id;
        this.days = days;
        this.durationMinutes =
                durationMinutes;
    }

    @Override
    public String toString() {

        return days.toString()
                + " ("
                + durationMinutes
                + " min)";
    }
}