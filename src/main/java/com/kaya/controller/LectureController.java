package com.kaya.controller;

<<<<<<< HEAD
import com.kaya.dto.request.LectureRequest;
=======
>>>>>>> 2f1def42acd0dd54877d40fc787cdfb45bbf7ddf
import com.kaya.dto.response.LectureResponse;
import com.kaya.model.Lecture;
import com.kaya.service.LectureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/lectures")
@RequiredArgsConstructor
public class LectureController {

    private final LectureService lectureService;

    @GetMapping
    public List<LectureResponse> getAll() {
        return lectureService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<LectureResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(lectureService.getById(id));
    }

    @PostMapping
<<<<<<< HEAD
    public ResponseEntity<LectureResponse> create(@Valid  @RequestBody LectureRequest request) {
=======
    public ResponseEntity<LectureResponse> create(@Valid  @RequestBody Lecture request) {
>>>>>>> 2f1def42acd0dd54877d40fc787cdfb45bbf7ddf
        return ResponseEntity.ok(lectureService.create(request));
    }

    @PutMapping("/{id}")
<<<<<<< HEAD
    public ResponseEntity<LectureResponse> update(@PathVariable Long id, @Valid @RequestBody LectureRequest request) {
=======
    public ResponseEntity<LectureResponse> update(@PathVariable Long id, @Valid @RequestBody Lecture request) {
>>>>>>> 2f1def42acd0dd54877d40fc787cdfb45bbf7ddf
        return ResponseEntity.ok(lectureService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        lectureService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
