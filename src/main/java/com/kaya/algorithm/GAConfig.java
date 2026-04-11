package com.kaya.algorithm;

// هذا الكلاس يطبق نمط (Configuration Object Pattern)
// وظيفته تجميع كل إعدادات الخوارزمية في مكان واحد نظيف
public class GAConfig {
    public int maxGenerations;
    public int populationSize;
    public int elitismCount;
    public int tournamentSize;
    public double initialMutationRate;

    // [جديد]: متغير للتحكم في قوة الطفرة (بنسبة مئوية)
    // يعني لو الطفرة حصلت، هنغير كام في المية من الكورسات المتعارضة؟ (20% ممتاز كبداية)
    public double mutationImpactRatio;

    public GAConfig() {
        // القيم الافتراضية (Default Values)
        this.maxGenerations = 400; // ممكن تزوده لـ 1000 بعدين لو لسه محتاج تحسين
        this.populationSize = 100; // ممكن تزوده لـ 200 لزيادة التنوع لو الجهاز يستحمل
        this.elitismCount = 2;
        this.tournamentSize = 5;
        this.initialMutationRate = 0.15;
        this.mutationImpactRatio = 0.10; // [جديد]: تغيير 10% من الكورسات المتعارضة في كل طفرة
    }
}