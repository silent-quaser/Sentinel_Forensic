package com.sentinelforensic.controller;

import com.sentinelforensic.detector.ThreatDetectionEngine;
import com.sentinelforensic.dto.DetectionResultDto;
import com.sentinelforensic.dto.ThreatDto;
import com.sentinelforensic.dto.UpdateThreatStatusRequest;
import com.sentinelforensic.model.ThreatSeverity;
import com.sentinelforensic.model.ThreatStatus;
import com.sentinelforensic.service.ThreatService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ThreatController {

    private final ThreatService threatService;
    private final ThreatDetectionEngine detectionEngine;

    public ThreatController(ThreatService threatService, ThreatDetectionEngine detectionEngine) {
        this.threatService = threatService;
        this.detectionEngine = detectionEngine;
    }

    @PostMapping("/investigations/{id}/detect")
    public ResponseEntity<DetectionResultDto> runDetection(@PathVariable Long id) {
        DetectionResultDto result = detectionEngine.runDetection(id);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/investigations/{id}/threats")
    public ResponseEntity<List<ThreatDto>> getInvestigationThreats(@PathVariable Long id) {
        List<ThreatDto> threats = threatService.getThreats(id, null, null, null, null);
        return ResponseEntity.ok(threats);
    }

    @GetMapping("/threats")
    public ResponseEntity<List<ThreatDto>> getAllThreats(
            @RequestParam(required = false) Long investigationId,
            @RequestParam(required = false) ThreatSeverity severity,
            @RequestParam(required = false) ThreatStatus status,
            @RequestParam(required = false) String ruleCode,
            @RequestParam(required = false) String username) {

        List<ThreatDto> threats = threatService.getThreats(investigationId, severity, status, ruleCode, username);
        return ResponseEntity.ok(threats);
    }

    @GetMapping("/threats/{id}")
    public ResponseEntity<ThreatDto> getThreatDetail(@PathVariable Long id) {
        ThreatDto threat = threatService.getThreatDetail(id);
        return ResponseEntity.ok(threat);
    }

    @PatchMapping("/threats/{id}/status")
    public ResponseEntity<ThreatDto> updateThreatStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateThreatStatusRequest request) {
        ThreatDto updated = threatService.updateThreatStatus(id, request.getStatus());
        return ResponseEntity.ok(updated);
    }
}
