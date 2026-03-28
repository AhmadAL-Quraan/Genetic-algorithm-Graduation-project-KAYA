package data;

import model.DayPattern;
import model.TimeSlot;

import java.util.ArrayList;
import java.util.List;

public class TimeSlotGenerator {

    public static List<TimeSlot> generate(
            int startHour,
            int endHour,
            List<DayPattern> patterns) {

        List<TimeSlot> slots =
                new ArrayList<>();

        int id = 0;

        int startMinutes =
                startHour * 60;

        int endMinutes =
                endHour * 60 + 30;

        for (DayPattern p : patterns) {

            int step =
                    p.durationMinutes;

            for (int t = startMinutes;
                 t + step <= endMinutes;
                 t += step) {

                slots.add(
                        new TimeSlot(
                                id,
                                t,
                                p.id,
                                p.durationMinutes,
                                false

                        )
                );

                id++;
            }
        }

        // Generate Online Slots

        int onlineStart = 16 * 60; // 4:00 PM
        int onlineEnd   = 22 * 60; // 10:00 PM

        int duration = 60;

        for (int time = onlineStart;
             time + duration <= onlineEnd;
             time += duration) {

            TimeSlot slot =
                    new TimeSlot(
                            id++,
                            time, // startMinutes
                            0,   // patternid
                            duration,
                            true // Online Slot
                    );

            slots.add(slot);

            slot = new TimeSlot(
                    id++,
                    time, // startMinutes
                    1, // pattern id
                    duration,
                    true // Online Slot
            );

            slots.add(slot);

        }

        System.out.println(
                "Online Slots Generated ✔"
        );

        return slots;
    }
}