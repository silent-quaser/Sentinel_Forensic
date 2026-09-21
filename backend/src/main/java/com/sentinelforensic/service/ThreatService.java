package com.sentinelforensic.service;

import com.sentinelforensic.dto.LogEntryDto;
import com.sentinelforensic.dto.ThreatDto;
import com.sentinelforensic.exception.ResourceNotFoundException;
import com.sentinelforensic.model.LogEntry;
import com.sentinelforensic.model.Threat;
import com.sentinelforensic.model.ThreatSeverity;
import com.sentinelforensic.model.ThreatStatus;
import com.sentinelforensic.repository.LogEntryRepository;
import com.sentinelforensic.repository.ThreatEvidenceRepository;
import com.sentinelforensic.repository.ThreatRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ThreatService {
    private static final Logger log = LoggerFactory.getLogger(ThreatService.class);

    private final ThreatRepository threatRepository;
    private final ThreatEvidenceRepository threatEvidenceRepository;
    private final LogEntryRepository logEntryRepository;

    public ThreatService(
            ThreatRepository threatRepository,
            ThreatEvidenceRepository threatEvidenceRepository,
            LogEntryRepository logEntryRepository) {
        this.threatRepository = threatRepository;
        this.threatEvidenceRepository = threatEvidenceRepository;
        this.logEntryRepository = logEntryRepository;
    }

    public List<ThreatDto> getThreats(
            Long investigationId,
            ThreatSeverity severity,
            ThreatStatus status,
            String ruleCode,
            String username) {

        List<Threat> threats = threatRepository.filterThreats(investigationId, severity, status, ruleCode, username);
        return threats.stream()
                .map(this::toThreatDtoWithoutEvidence)
                .collect(Collectors.toList());
    }

    public ThreatDto getThreatDetail(Long id) {
        Threat threat = threatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Threat", "id", id));

        List<Long> logIds = threatEvidenceRepository.findLogEntryIdsByThreatId(threat.getId());
        List<LogEntry> evidenceLogs = logEntryRepository.findAllById(logIds);

        ThreatDto dto = toThreatDtoWithoutEvidence(threat);

        List<LogEntryDto> evDtos = evidenceLogs.stream().map(e -> {
            LogEntryDto led = new LogEntryDto();
            led.setId(e.getId());
            led.setInvestigationId(e.getInvestigationId());
            led.setTimestamp(e.getTimestamp());
            led.setEventType(e.getEventType());
            led.setUsername(e.getUsername());
            led.setSource(e.getSource());
            led.setSeverity(e.getSeverity());
            led.setDescription(e.getDescription());
            led.setRawMessage(e.getRawMessage());
            return led;
        }).collect(Collectors.toList());

        dto.setEvidenceEvents(evDtos);
        return dto;
    }

    @Transactional
    public ThreatDto updateThreatStatus(Long id, ThreatStatus newStatus) {
        Threat threat = threatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Threat", "id", id));

        log.info("Updating threat ID {} status from {} to {}", id, threat.getStatus(), newStatus);
        threat.setStatus(newStatus);
        Threat saved = threatRepository.save(threat);
        return getThreatDetail(saved.getId());
    }

    public ThreatDto toThreatDtoWithoutEvidence(Threat threat) {
        ThreatDto dto = new ThreatDto();
        dto.setId(threat.getId());
        dto.setInvestigationId(threat.getInvestigationId());
        dto.setRuleCode(threat.getRuleCode());
        dto.setTitle(threat.getTitle());
        dto.setDescription(threat.getDescription());
        dto.setExplanation(threat.getExplanation());
        dto.setSeverity(threat.getSeverity());
        dto.setScore(threat.getScore());
        dto.setStatus(threat.getStatus());
        dto.setFirstObserved(threat.getFirstObserved());
        dto.setLastObserved(threat.getLastObserved());
        dto.setDetectedAt(threat.getDetectedAt());
        dto.setAffectedUser(threat.getAffectedUser());
        dto.setAffectedIp(threat.getAffectedIp());
        dto.setEscalationReason(threat.getEscalationReason());
        dto.setScoreBreakdown(threat.getScoreBreakdown());
        return dto;
    }
}
