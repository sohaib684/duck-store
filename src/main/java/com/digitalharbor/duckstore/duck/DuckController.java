package com.digitalharbor.duckstore.duck;

import com.digitalharbor.duckstore.duck.dto.CreateDuckRequest;
import com.digitalharbor.duckstore.duck.dto.UpdateDuckRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ducks")
@CrossOrigin(origins = "http://localhost:5173")
public class DuckController {

    private final DuckService duckService;

    public DuckController(DuckService duckService) {
        this.duckService = duckService;
    }

    @GetMapping
    public List<Duck> getAllDucks() {
        return duckService.getAllDucks();
    }

    @PostMapping
    public ResponseEntity<Duck> addDuck(
            @Valid @RequestBody CreateDuckRequest request) {
        return ResponseEntity.ok(duckService.addDuck(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Duck> updateDuck(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDuckRequest request) {
        return ResponseEntity.ok(duckService.updateDuck(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDuck(
            @PathVariable Long id) {
        duckService.deleteDuck(id);
        return ResponseEntity.noContent().build();
    }
}