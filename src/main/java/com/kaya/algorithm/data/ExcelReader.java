package com.kaya.algorithm.data;

import org.dhatim.fastexcel.reader.ReadableWorkbook;
import org.dhatim.fastexcel.reader.Row;
import org.dhatim.fastexcel.reader.Sheet;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.stream.Stream;

public class ExcelReader {

    public static ArrayList<String[]> read(String filePath, ArrayList<Integer> columnIndices) {
        ArrayList<String[]> rows = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(filePath);
             ReadableWorkbook wb = new ReadableWorkbook(fis)) {

            Sheet sheet = wb.getFirstSheet();

            try (Stream<Row> stream = sheet.openStream()) {
                stream.skip(1).forEach(row -> {
                    String[] cells = new String[columnIndices.size()];
                    for (int i = 0; i < columnIndices.size(); i++) {
                        cells[i] = row.getCellText(columnIndices.get(i));
                    }
                    rows.add(cells);
                });
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to read Excel file: " + filePath, e);
        }

        return rows;
    }
}