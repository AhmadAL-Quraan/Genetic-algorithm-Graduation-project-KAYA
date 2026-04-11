package com.kaya.algorithm;

import com.kaya.model.TimeTable;

import java.util.ArrayList;
import java.util.Random;

public class IslandManager {

    // this function is not used currently but may be used in future for optimization.
    public static ArrayList<TimeTable> islandsMerge(ArrayList<ArrayList<TimeTable>> islands) {
        Random rand = new Random();
        ArrayList<TimeTable> finalPopulation = new ArrayList<>();

        int min = 1;
        int max = islands.get(0).size() / 5;
        int index = 0;
        for (ArrayList<TimeTable> island : islands) {
            index = 0;
            int number = rand.nextInt(max - min + 1) + min;
            while (number > 0) {
                finalPopulation.add(island.get(index));
                index++;
                number--;
            }
        }
        while(finalPopulation.size() < islands.get(0).size())
            finalPopulation.add(islands.get(4).get(index++));

        return finalPopulation;
    }
}