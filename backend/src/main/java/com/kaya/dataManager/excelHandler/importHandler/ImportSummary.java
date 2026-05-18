package com.kaya.dataManager.excelHandler.importHandler;

import java.util.List;

public record ImportSummary(
        int rowsProcessed,
        int rowsSkipped,
        int coursesCreated,
        int roomsCreated,
        int timeSlotsCreated,
        int lecturesCreated,
        int instructorsCreated,
        List<String> warnings
) {}