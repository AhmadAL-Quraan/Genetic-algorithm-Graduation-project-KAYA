package com.kaya.dataManager.excelHandler.importHandler;

import com.kaya.model.TimeSlot;
import com.kaya.model.enums.TeachingMethod;
import org.dhatim.fastexcel.reader.Cell;
import org.dhatim.fastexcel.reader.Row;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

@Component
public class ImportHelper {

    public String cell(Row row, int col) {
        if (col >= row.getCellCount()) return "";
        Cell c = row.getCell(col);
        if (c == null || c.getValue() == null) return "";
        return c.getValue().toString().trim();
    }

    public boolean isX(Row row, int col) {
        return cell(row, col).equalsIgnoreCase("X") || cell(row, col).equals("×");
    }

    public String normalize(String s) {
        return s == null ? "" : s.trim().toLowerCase();
    }

    public String courseKey(String symbol, String number) {
        return normalize(symbol) + "|" + normalize(number);
    }

    public String roomKey(String building, String number) {
        return normalize(building) + "|" + normalize(number);
    }

    public String slotKey(TimeSlot ts) {
        List<String> days = ts.getDays().stream()
                .sorted(Comparator.comparingInt(DayOfWeek::getValue))
                .map(Enum::name)
                .toList();

        String method = ts.getTeachingMethod() != null ? ts.getTeachingMethod().name() : "null";

        return ts.getStartTime() + "-" + ts.getEndTime() + "-" + days + "-" + method;
    }

    public TeachingMethod parseMethod(String arabic) {
        if (arabic == null) return TeachingMethod.IN_PERSON;

        return switch (arabic.trim()) {
            case "إلكتروني", "الكتروني", "أونلاين", "اونلاين" -> TeachingMethod.ONLINE;
            case "مدمج", "هجين" -> TeachingMethod.BLENDED;
            default -> TeachingMethod.IN_PERSON;
        };
    }

    public LocalTime excelFractionToTime(double fraction) {
        int totalMinutes = (int) Math.round(fraction * 24 * 60);
        return LocalTime.of(totalMinutes / 60, totalMinutes % 60);
    }
}