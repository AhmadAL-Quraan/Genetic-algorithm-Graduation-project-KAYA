package com.kaya.controller;

<<<<<<< HEAD
import com.kaya.dto.request.RoomRequest;
=======
>>>>>>> 2f1def42acd0dd54877d40fc787cdfb45bbf7ddf
import com.kaya.dto.response.RoomResponse;
import com.kaya.model.Room;
import com.kaya.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @GetMapping
    public List<RoomResponse> getAll() {
        return roomService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoomResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.getById(id));
    }

    @PostMapping
<<<<<<< HEAD
    public ResponseEntity<RoomResponse> create(@RequestBody RoomRequest request) {
=======
    public ResponseEntity<RoomResponse> create(@RequestBody Room request) {
>>>>>>> 2f1def42acd0dd54877d40fc787cdfb45bbf7ddf
        return ResponseEntity.ok(roomService.create(request));
    }

    @PutMapping("/{id}")
<<<<<<< HEAD
    public ResponseEntity<RoomResponse> update(@PathVariable Long id, @RequestBody RoomRequest request) {
=======
    public ResponseEntity<RoomResponse> update(@PathVariable Long id, @RequestBody Room request) {
>>>>>>> 2f1def42acd0dd54877d40fc787cdfb45bbf7ddf
        return ResponseEntity.ok(roomService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        roomService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
