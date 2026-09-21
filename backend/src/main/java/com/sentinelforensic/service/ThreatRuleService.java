package com.sentinelforensic.service;

import com.sentinelforensic.dto.ThreatRuleDto;
import com.sentinelforensic.dto.UpdateThreatRuleRequest;
import com.sentinelforensic.exception.ResourceNotFoundException;
import com.sentinelforensic.model.ThreatRule;
import com.sentinelforensic.repository.ThreatRuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ThreatRuleService {
    private static final Logger log = LoggerFactory.getLogger(ThreatRuleService.class);

    private final ThreatRuleRepository ruleRepository;

    public ThreatRuleService(ThreatRuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    public List<ThreatRuleDto> getAllRules() {
        return ruleRepository.findAllByOrderByRuleCodeAsc().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public ThreatRuleDto getRuleByCode(String ruleCode) {
        ThreatRule rule = ruleRepository.findByRuleCode(ruleCode)
                .orElseThrow(() -> new ResourceNotFoundException("ThreatRule", "ruleCode", ruleCode));
        return toDto(rule);
    }

    @Transactional
    public ThreatRuleDto updateRule(Long id, UpdateThreatRuleRequest request) {
        ThreatRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ThreatRule", "id", id));

        if (request.getEnabled() != null) {
            rule.setEnabled(request.getEnabled());
        }
        if (request.getThreshold() != null && request.getThreshold() > 0) {
            rule.setThreshold(request.getThreshold());
        }
        if (request.getTimeWindowSeconds() != null && request.getTimeWindowSeconds() > 0) {
            rule.setTimeWindowSeconds(request.getTimeWindowSeconds());
        } else if (request.getTimeWindowMinutes() != null && request.getTimeWindowMinutes() > 0) {
            rule.setTimeWindowMinutes(request.getTimeWindowMinutes());
        }
        if (request.getSeverity() != null) {
            rule.setSeverity(request.getSeverity());
        }

        ThreatRule saved = ruleRepository.save(rule);
        log.info("Updated threat rule {}: enabled={}, threshold={}, window={}s",
                rule.getRuleCode(), saved.getEnabled(), saved.getThreshold(), saved.getTimeWindowSeconds());
        return toDto(saved);
    }

    private ThreatRuleDto toDto(ThreatRule rule) {
        ThreatRuleDto dto = new ThreatRuleDto();
        dto.setId(rule.getId());
        dto.setRuleCode(rule.getRuleCode());
        dto.setName(rule.getName());
        dto.setDescription(rule.getDescription());
        dto.setSeverity(rule.getSeverity());
        dto.setThreshold(rule.getThreshold());
        dto.setTimeWindowSeconds(rule.getTimeWindowSeconds());
        dto.setTimeWindowMinutes(rule.getTimeWindowMinutes());
        dto.setEnabled(rule.getEnabled());
        dto.setCreatedAt(rule.getCreatedAt());
        return dto;
    }
}
