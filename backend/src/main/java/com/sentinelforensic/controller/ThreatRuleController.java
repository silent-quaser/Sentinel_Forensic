package com.sentinelforensic.controller;

import com.sentinelforensic.dto.ThreatRuleDto;
import com.sentinelforensic.dto.UpdateThreatRuleRequest;
import com.sentinelforensic.service.ThreatRuleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rules")
public class ThreatRuleController {

    private final ThreatRuleService ruleService;

    public ThreatRuleController(ThreatRuleService ruleService) {
        this.ruleService = ruleService;
    }

    @GetMapping
    public ResponseEntity<List<ThreatRuleDto>> getAllRules() {
        List<ThreatRuleDto> rules = ruleService.getAllRules();
        return ResponseEntity.ok(rules);
    }

    @GetMapping("/{code}")
    public ResponseEntity<ThreatRuleDto> getRuleByCode(@PathVariable String code) {
        ThreatRuleDto rule = ruleService.getRuleByCode(code);
        return ResponseEntity.ok(rule);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ThreatRuleDto> updateRule(
            @PathVariable Long id,
            @RequestBody UpdateThreatRuleRequest request) {
        ThreatRuleDto updated = ruleService.updateRule(id, request);
        return ResponseEntity.ok(updated);
    }
}
