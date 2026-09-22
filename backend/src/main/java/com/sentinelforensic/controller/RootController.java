package com.sentinelforensic.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class RootController {

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> getRoot() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("application", "SentinelForensic – Digital Incident Triage API");
        info.put("status", "UP");
        info.put("health", "/api/health");
        info.put("documentation", "https://github.com/silent-quaser/Sentinel_Forensic");
        info.put("version", "1.0.0-RELEASE");
        return ResponseEntity.ok(info);
    }
}