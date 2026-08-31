package ru.amvera.demo;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {
    private final ItemStore store;

    public ApiController(ItemStore store) {
        this.store = store;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("ok", true, "framework", "Spring Boot", "storage", store.dataFile().toString());
    }

    @GetMapping("/items")
    public Map<String, Object> items() throws IOException {
        List<Item> items = store.read();
        Collections.reverse(items);
        return Map.of("items", items, "count", items.size());
    }

    @PostMapping("/items")
    public ResponseEntity<?> create(@RequestBody Map<String, Object> data) throws IOException {
        String name = String.valueOf(data.getOrDefault("name", "")).trim();
        if (name.isEmpty() || name.length() > 120) return ResponseEntity.badRequest().body(Map.of("error", "Name must contain from 1 to 120 characters"));
        return ResponseEntity.status(201).body(Map.of("item", store.create(name)));
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<?> delete(@PathVariable long id) throws IOException {
        if (!store.delete(id)) return ResponseEntity.status(404).body(Map.of("error", "Item not found"));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("deleted", true);
        result.put("id", id);
        return ResponseEntity.ok(result);
    }
}
