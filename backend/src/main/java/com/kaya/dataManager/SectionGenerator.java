package com.kaya.dataManager;

import com.kaya.model.Lecture;
import com.kaya.model.TimeTable;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class SectionGenerator {

    public static void generate(TimeTable table) {
        List<Lecture> lectures = table.getLectures();
        if (lectures == null || lectures.isEmpty()) {
            return;
        }
        sortLectures(lectures);

        int number = 1;
        lectures.get(0).setSectionNumber(1);
        for (int i = 1; i < lectures.size(); i++) {
            if (Objects.equals(lectures.get(i).getCourse().getCourseSymbol(),
                    lectures.get(i - 1).getCourse().getCourseSymbol())
                    && Objects.equals(lectures.get(i).getCourse().getCourseNumber(),
                    lectures.get(i - 1).getCourse().getCourseNumber())) {
                number++;
            } else {
                number = 1;
            }
            lectures.get(i).setSectionNumber(number);
        }

        table.setLectures(lectures);
    }

    private static void sortLectures(List<Lecture> lectures) {
        lectures.sort(Comparator
                .comparing((Lecture l) -> l.getCourse().getCourseSymbol())
                .thenComparing(l -> l.getCourse().getCourseNumber())
        );
    }
}
