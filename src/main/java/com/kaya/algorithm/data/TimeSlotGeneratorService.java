package com.kaya.algorithm.data;

import com.kaya.model.TimeSlot;
import com.kaya.model.TimeSlotType;
import com.kaya.repository.TimeSlotRepository;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service // Annotated as Service so Spring Boot detects it and manages its lifecycle as a Singleton Bean
public class TimeSlotGeneratorService {

    private final TimeSlotRepository timeSlotRepository;

    // Dependency Injection for the Repository to interact with the database
    public TimeSlotGeneratorService(TimeSlotRepository timeSlotRepository) {
        this.timeSlotRepository = timeSlotRepository;
    }

    /**
     * Takes the rules specified by the user, generates the time slots,
     * and saves them directly to the database.
     * NOTE: This method cannot be static because it relies on the injected Spring Data Repository.
     */
    public List<TimeSlot> generateAndSaveTimeSlots(
            Set<DayOfWeek> days,
            LocalTime startTime,
            LocalTime endTime,
            int durationMinutes,
            TimeSlotType type) {

        List<TimeSlot> generatedSlots = new ArrayList<>();
        LocalTime currentStart = startTime;

        // Loop as long as the end time of the current generated slot does not exceed the total end time
        while (!currentStart.plusMinutes(durationMinutes).isAfter(endTime)) {

            // Calculate the end time of the current time slot
            LocalTime currentEnd = currentStart.plusMinutes(durationMinutes);

            // Create a new TimeSlot object (ID is null so the database auto-generates it)
            TimeSlot newSlot = new TimeSlot(null, currentStart, currentEnd, days, type);

            // Add it to the list
            generatedSlots.add(newSlot);

            // Move the start time to the next slot (e.g., if 8-9, next iteration starts at 9)
            currentStart = currentEnd;
        }

        // Save all generated slots to the database in a single batch query (highly optimized)
        return timeSlotRepository.saveAll(generatedSlots);
    }
}