package com.kaya.dataManager.dataGenerator;

import com.kaya.algorithm.GAConfig;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GAConfigMapper {

    public GAConfig map(Map<String, Object> configMap, GAConfig config) {
        if (configMap == null) return config;

        if (configMap.containsKey("maxGenerations"))
            config.maxGenerations = toInt(configMap.get("maxGenerations"), config.maxGenerations);
        if (configMap.containsKey("populationSize"))
            config.populationSize = toInt(configMap.get("populationSize"), config.populationSize);
        if (configMap.containsKey("elitismRatio"))
            config.elitismRatio = toDouble(configMap.get("elitismRatio"), config.elitismRatio);
        if (configMap.containsKey("tournamentSize"))
            config.tournamentSize = toInt(configMap.get("tournamentSize"), config.tournamentSize);
        if (configMap.containsKey("initialMutationRate"))
            config.initialMutationRate = toDouble(configMap.get("initialMutationRate"), config.initialMutationRate);
        if (configMap.containsKey("mutationImpactRatio"))
            config.mutationImpactRatio = toDouble(configMap.get("mutationImpactRatio"), config.mutationImpactRatio);
        if (configMap.containsKey("stagnationToleranceRatio"))
            config.stagnationToleranceRatio = toDouble(configMap.get("stagnationToleranceRatio"), config.stagnationToleranceRatio);
        if (configMap.containsKey("numIslands"))
            config.numIslands = toInt(configMap.get("numIslands"), config.numIslands);
        if (configMap.containsKey("migrationInterval"))
            config.migrationInterval = toInt(configMap.get("migrationInterval"), config.migrationInterval);
        if (configMap.containsKey("migrationRate"))
            config.migrationRate = toInt(configMap.get("migrationRate"), config.migrationRate);

        return config;
    }

    public boolean resolveUseIslandModel(Map<String, Object> configMap) {
        if (configMap == null) return false;
        return toBoolean(configMap.get("useIslandModel"), false);
    }

    private int toInt(Object val, int def) {
        if (val instanceof Number) return ((Number) val).intValue();
        try { return Integer.parseInt(val.toString()); } catch (Exception e) { return def; }
    }

    private double toDouble(Object val, double def) {
        if (val instanceof Number) return ((Number) val).doubleValue();
        try { return Double.parseDouble(val.toString()); } catch (Exception e) { return def; }
    }

    private boolean toBoolean(Object val, boolean def) {
        if (val instanceof Boolean) return (Boolean) val;
        try { return Boolean.parseBoolean(val.toString()); } catch (Exception e) { return def; }
    }
}