package com.kaya.algorithm.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class RowsToJSON {

    private static final ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    public static String convert(ArrayList<String[]> rows, ArrayList<String> columnNames) {
        ArrayList<Map<String, String>> result = new ArrayList<>();

        for (String[] row : rows) {
            Map<String, String> map = new LinkedHashMap<>();
            for (int i = 0; i < columnNames.size(); i++) {
                map.put(columnNames.get(i), row[i]);
            }
            result.add(map);
        }

        try {
            return mapper.writeValueAsString(result);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert rows to JSON", e);
        }
    }
}