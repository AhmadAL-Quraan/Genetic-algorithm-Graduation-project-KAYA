package com.kaya.dataManager.excelHandler.exportHandler;

import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
public class DayPatternHelper {

    public static final Set<DayOfWeek> STT = Set.of(
            DayOfWeek.SUNDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.THURSDAY
    );

    public static final Set<DayOfWeek> MWS = Set.of(
            DayOfWeek.MONDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.SATURDAY
    );

    public static final Set<DayOfWeek> MW = Set.of(
            DayOfWeek.MONDAY,
            DayOfWeek.WEDNESDAY
    );

    public Set<DayOfWeek> canonicalGroup(Set<DayOfWeek> days) {
        if (days == null || days.isEmpty()) return days;
        if (days.equals(STT) || days.equals(MWS) || days.equals(MW)) return days;

        if (STT.containsAll(days)) return STT;
        if (days.contains(DayOfWeek.SATURDAY) && MWS.containsAll(days)) return MWS;
        if (MW.containsAll(days)) return MW;

        return days;
    }

    public int groupOf(Set<DayOfWeek> days) {
        Set<DayOfWeek> g = canonicalGroup(days);
        if (g == null || g.isEmpty()) return 4;
        if (g.equals(STT)) return 1;
        if (g.equals(MWS)) return 2;
        if (g.equals(MW)) return 3;
        return 4;
    }

    public String formatDaysFull(Set<DayOfWeek> days) {
        if (days == null || days.isEmpty()) return "";

        List<DayOfWeek> order = List.of(
                DayOfWeek.SATURDAY,
                DayOfWeek.SUNDAY,
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY
        );

        return order.stream()
                .filter(days::contains)
                .map(d -> d.getDisplayName(TextStyle.FULL, Locale.ENGLISH).toUpperCase(Locale.ENGLISH))
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
    }
}