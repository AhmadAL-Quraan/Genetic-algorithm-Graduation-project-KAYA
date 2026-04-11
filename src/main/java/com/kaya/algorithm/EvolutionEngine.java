package com.kaya.algorithm;

import com.kaya.model.Lecture;
import com.kaya.model.Room;
import com.kaya.model.TimeSlot;
import com.kaya.model.TimeTable;
import com.kaya.model.enums.RoomType;
import com.kaya.model.enums.TeachingMethod;

import java.util.*;

// الكلاس ده هو المحرك الجيني (Genetic Engine) اللي بيدير عملية التطور بالكامل
public class EvolutionEngine {
    private final GAConfig config;
    private double currentMutationRate;

    // الـ Constructor بياخد الإعدادات (Config) عشان يشتغل بيها
    public EvolutionEngine(GAConfig config) {
        this.config = config;
        this.currentMutationRate = config.initialMutationRate;
    }

    // [تم التعديل]: استخدام Enums في تعريف الـ Pools بدل String
    public ArrayList<TimeTable> initializePopulation(ArrayList<Lecture> lectures,
                                                     Map<TeachingMethod, HashSet<TimeSlot>> timePools,
                                                     Map<RoomType, HashSet<Room>> roomPools) {

        ArrayList<TimeTable> population = new ArrayList<>();

        // بنعمل عدد من الجداول العشوائية بناءً على حجم الـ Population المطلوب
        for (int i = 0; i < config.populationSize; i++) {
            ArrayList<Lecture> individualClasses = new ArrayList<>();
            for (Lecture c : lectures) {
                // بنعمل نسخة جديدة من الكورس لكل جدول، وبنسيب القاعة والوقت null عشان الـ Initializer هو اللي هيحطهم
                individualClasses.add(new Lecture(c.getId(), c.getCourse(), null, null, c.getSectionNumber(), c.getInstructor()));
            }
            TimeTable tt = new TimeTable(individualClasses);

            // تهيئة الجدول عشوائياً بأوقات وقاعات مناسبة
            TimeTableInitializer.initializeRandomly(tt, timePools, roomPools);
            population.add(tt);
        }
        return population;
    }

    // [تم التعديل]: استخدام Enums في الـ Pools
    public ArrayList<TimeTable> evolveGenerations(ArrayList<TimeTable> population,
                                                  Map<TeachingMethod, HashSet<TimeSlot>> timePools,
                                                  Map<RoomType, HashSet<Room>> roomPools) {

        int unevolvedGenerations = 0;

        // اللوب الأساسية للأجيال
        for (int gen = 1; gen <= config.maxGenerations; gen++) {

            // 1. ترتيب الجداول من الأحسن (أقرب للصفر) للأسوأ
            population.sort((a, b) -> Long.compare(b.getFitness(), a.getFitness()));
            System.out.println("Generation " + gen + " | Best Fitness: " + population.get(0).getFitness());

            // لو وصلنا لجدول مثالي (مفيش ولا تعارض)، بنوقف الخوارزمية عشان نوفر وقت
            if (population.get(0).getFitness() == 0) {
                System.out.println("--- Perfect Schedule Found! ---");
                break;
            }

            ArrayList<TimeTable> nextGen = new ArrayList<>();

            // 2. النخبوية (ELITISM): بناخد أحسن جداول زي ما هما للجيل الجاي عشان منخسرهمش
            for (int i = 0; i < config.elitismCount; i++) {
                nextGen.add(population.get(i));
            }

            // 3. التكاثر (REPRODUCTION): بنملى بقية الجيل الجديد
            while (nextGen.size() < config.populationSize) {
                // اختيار أب وأم باستخدام البطولات (Tournament)
                TimeTable p1 = Selection.tournamentSelection(population, config.tournamentSize);
                TimeTable p2 = Selection.tournamentSelection(population, config.tournamentSize);

                // التزاوج (Crossover) لإنتاج طفل جديد
                TimeTable child = GeneticOperators.crossover(p1, p2);

                // حساب الفيتنس للطفل عشان الطفرة تعرف فين التعارضات
                FitnessCalculator.calculateFitness(child);

                // 4. الطفرة (MUTATION): احتمال إن الطفل يحصله تعديل عشوائي لكسر التعارضات
                if (Math.random() < currentMutationRate) {
                    GeneticOperators.mutate(child, timePools, roomPools, config.mutationImpactRatio);
                }

                // حساب الفيتنس النهائي بعد الطفرة وإضافته للجيل الجديد
                FitnessCalculator.calculateFitness(child);
                nextGen.add(child);
            }

            // 5. الطفرة التكيفية (Adaptive Mutation Logic)
            // لو الفيتنس مش بيتحسن (الخوارزمية علقت في Local Optima)، بنزود قوة الطفرة
            if (population.get(0).getFitness() >= nextGen.get(0).getFitness()) {
                unevolvedGenerations++;
            } else {
                unevolvedGenerations = 0;
                currentMutationRate = config.initialMutationRate; // بنرجعها للنسبة الطبيعية لو اتحسنت
            }

            // لو بقالها 50 جيل مش بتتحسن، نزود الطفرة بـ 5% (بحد أقصى 50%)
            if (unevolvedGenerations > 0 && unevolvedGenerations % 50 == 0 && currentMutationRate < 0.5) {
                currentMutationRate += 0.05;
            }

            population = nextGen;
        }

        // ترتيب الجيل الأخير عشان نرجع أحسن جدول في أول إندكس
        population.sort((a, b) -> Long.compare(b.getFitness(), a.getFitness()));
        return population;
    }
}