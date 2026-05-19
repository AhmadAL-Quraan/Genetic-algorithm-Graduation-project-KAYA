package com.kaya.controller;

import com.kaya.dto.response.ConflictResponse;
import com.kaya.service.ConflictService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/conflicts")
@RequiredArgsConstructor
public class ConflictController {

    private final ConflictService conflictService;

    @GetMapping
    public List<ConflictResponse> getConflicts() {
        return conflictService.getLatestConflicts();
    }
}