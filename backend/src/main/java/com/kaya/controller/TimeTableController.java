package com.kaya.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaya.algorithm.GAConfig;
import com.kaya.dto.request.TimeTableRequest;
import com.kaya.dto.response.GenerationProgress;
import com.kaya.dto.response.TimeTableResponse;
import com.kaya.service.TimeTableService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/time-table")
@RequiredArgsConstructor
public class TimeTableController {

    private final TimeTableService timeTableService;
    private final ObjectMapper objectMapper;
    private static final ExecutorService executor = Executors.newCachedThreadPool();

    @GetMapping
    public List<TimeTableResponse> getAll() {
        return timeTableService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TimeTableResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(timeTableService.getById(id));
    }

    @PostMapping
    public ResponseEntity<TimeTableResponse> create(@RequestBody TimeTableRequest request) {
        return ResponseEntity.ok(timeTableService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TimeTableResponse> update(@PathVariable Long id, @RequestBody TimeTableRequest request) {
        return ResponseEntity.ok(timeTableService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        timeTableService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/generate")
    public ResponseEntity<TimeTableResponse> generate(@RequestBody(required = false) Map<String, Object> body) {
        GAConfig cfg = buildConfig(body);
        return ResponseEntity.ok(timeTableService.generate(cfg));
    }

    /**
     * SSE endpoint: streams GenerationProgress events as the GA evolves, then
     * sends a final "complete" event carrying the persisted TimeTableResponse JSON.
     */
    @PostMapping(value = "/generate-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter generateStream(@RequestBody(required = false) Map<String, Object> body) {
        GAConfig cfg = buildConfig(body);
        SseEmitter emitter = new SseEmitter(600_000L);

        executor.submit(() -> {
            try {
                TimeTableResponse result = timeTableService.generateWithProgress(cfg, progress -> {
                    try {
                        String json = objectMapper.writeValueAsString(progress);
                        emitter.send(SseEmitter.event().name("progress").data(json));
                    } catch (Exception ignored) {}
                });

                String resultJson = objectMapper.writeValueAsString(result);
                emitter.send(SseEmitter.event().name("complete").data(resultJson));
                emitter.complete();
            } catch (Exception e) {
                try {
                    emitter.send(SseEmitter.event().name("error").data(e.getMessage()));
                } catch (Exception ignored) {}
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    private GAConfig buildConfig(Map<String, Object> body) {
        GAConfig cfg = new GAConfig();
        if (body != null) {
            if (body.get("maxGenerations") instanceof Number n) cfg.maxGenerations = n.intValue();
            if (body.get("populationSize") instanceof Number n) cfg.populationSize = n.intValue();
            if (body.get("elitismCount") instanceof Number n) cfg.elitismCount = n.intValue();
            if (body.get("tournamentSize") instanceof Number n) cfg.tournamentSize = n.intValue();
            if (body.get("initialMutationRate") instanceof Number n) cfg.initialMutationRate = n.doubleValue();
            if (body.get("mutationImpactRatio") instanceof Number n) cfg.mutationImpactRatio = n.doubleValue();
        }
        return cfg;
    }
}
