package edu.info5100.questapp.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "message", "QuestApp API is running"));
    }

    @GetMapping("/hw")
    public ResponseEntity<Map<String, Object>> helloWorld(@RequestParam Map<String, String> params) {
        return ResponseEntity.ok(Map.of("message", "hello world", "params", params));
    }
}
