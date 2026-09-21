package com.sentinelforensic.detector;

import com.sentinelforensic.dto.DetectionResultDto;
import com.sentinelforensic.dto.LogEntryDto;
import com.sentinelforensic.dto.ThreatDto;
import com.sentinelforensic.model.*;
import com.sentinelforensic.repository.LogEntryRepository;
import com.sentinelforensic.repository.ThreatEvidenceRepository;
import com.sentinelforensic.repository.ThreatRepository;
import com.sentinelforensic.repository.ThreatRuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ThreatDetectionEngine {
    private static final Logger log = LoggerFactory.getLogger(ThreatDetectionEngine.class);

    private final List<ThreatRuleEvaluator> evaluators;
    private final ThreatRuleRepository ruleRepository;
    private final ThreatRepository threatRepository;
    private final ThreatEvidenceRepository threatEvidenceRepository;
    private final LogEntryRepository logEntryRepository;

    public ThreatDetectionEngine(
            List<ThreatRuleEvaluator> evaluators,
            ThreatRuleRepository ruleRepository,
            ThreatRepository threatRepository,
            ThreatEvidenceRepository threatEvidenceRepository,
            LogEntryRepository logEntryRepository) {
        this.evaluators = evaluators;
        this.ruleRepository = ruleRepository;
        this.threatRepository = threatRepository;
        this.threatEvidenceRepository = threatEvidenceRepository;
        this.logEntryRepository = logEntryRepository;
    }

    @Transactional
    public DetectionResultDto runDetection(Long investigationId) {
        log.info("Starting threat detection engine for investigationId: {}", investigationId);

        List<LogEntry> logs = logEntryRepository.findByInvestigationIdOrderByTimestampAscIdAsc(investigationId);
        if (logs.isEmpty()) {
            return new DetectionResultDto(0, 0, 0, "No log events found for investigation.");
        }

        // Fetch configured threat rules from DB
        Map<String, ThreatRule> ruleConfigMap = ruleRepository.findAll().stream()
                .collect(Collectors.toMap(ThreatRule::getRuleCode, r -> r, (a, b) -> a));

        List<ThreatDetectionResult> candidateResults = new ArrayList<>();
        Set<Long> bruteForceCoveredLogIds = new HashSet<>();

        int rulesEvaluated = 0;

        // Run evaluators in prioritized order (BRUTE_FORCE_001 first to collect covered evidence)
        List<ThreatRuleEvaluator> sortedEvaluators = new ArrayList<>(evaluators);
        sortedEvaluators.sort((a, b) -> {
            if ("BRUTE_FORCE_001".equals(a.getRuleCode())) return -1;
            if ("BRUTE_FORCE_001".equals(b.getRuleCode())) return 1;
            return a.getRuleCode().compareTo(b.getRuleCode());
        });

        for (ThreatRuleEvaluator evaluator : sortedEvaluators) {
            String code = evaluator.getRuleCode();
            ThreatRule config = ruleConfigMap.get(code);

            // Skip if disabled in database config
            if (config != null && !config.getEnabled()) {
                log.debug("Skipping disabled rule: {}", code);
                continue;
            }

            rulesEvaluated++;
            List<ThreatDetectionResult> ruleResults = evaluator.evaluate(logs, config);

            for (ThreatDetectionResult r : ruleResults) {
                // Rule 2 / AUTH_FAILURE_001 suppression requirement:
                // Do not duplicate exact evidence already detected by BRUTE_FORCE_001
                if ("AUTH_FAILURE_001".equals(code)) {
                    List<Long> evIds = r.getEvidenceEvents().stream().map(LogEntry::getId).filter(Objects::nonNull).toList();
                    if (!evIds.isEmpty() && bruteForceCoveredLogIds.containsAll(evIds)) {
                        log.debug("Suppressing AUTH_FAILURE_001 alert for user {} as evidence is covered by brute-force detection", r.getThreat().getAffectedUser());
                        continue;
                    }
                }

                if ("BRUTE_FORCE_001".equals(code)) {
                    for (LogEntry ev : r.getEvidenceEvents()) {
                        if (ev.getId() != null) {
                            bruteForceCoveredLogIds.add(ev.getId());
                        }
                    }
                }

                candidateResults.add(r);
            }
        }

        // Deterministic duplicate suppression against already persisted threats
        List<Threat> existingThreats = threatRepository.findByInvestigationIdOrderByDetectedAtDesc(investigationId);
        int threatsCreated = 0;
        int existingUnchanged = 0;
        List<ThreatDto> newlyDetectedDtos = new ArrayList<>();

        for (ThreatDetectionResult candidate : candidateResults) {
            Threat threat = candidate.getThreat();
            List<LogEntry> evidence = candidate.getEvidenceEvents();
            Set<Long> candidateEvidenceIds = evidence.stream()
                    .map(LogEntry::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            boolean isDuplicate = false;

            for (Threat existing : existingThreats) {
                if (existing.getRuleCode().equals(threat.getRuleCode()) &&
                    Objects.equals(existing.getAffectedUser(), threat.getAffectedUser())) {

                    // Compare evidence IDs
                    List<Long> existingEvIds = threatEvidenceRepository.findLogEntryIdsByThreatId(existing.getId());
                    Set<Long> existingEvSet = new HashSet<>(existingEvIds);

                    // If exact evidence or overlapping window within 5 minutes
                    if (!candidateEvidenceIds.isEmpty() && existingEvSet.equals(candidateEvidenceIds)) {
                        isDuplicate = true;
                        break;
                    }

                    if (existing.getFirstObserved() != null && threat.getFirstObserved() != null) {
                        if (existing.getFirstObserved().equals(threat.getFirstObserved()) &&
                            existing.getLastObserved().equals(threat.getLastObserved())) {
                            isDuplicate = true;
                            break;
                        }
                    }
                }
            }

            if (isDuplicate) {
                existingUnchanged++;
            } else {
                threat.setInvestigationId(investigationId);
                threat.setDetectedAt(LocalDateTime.now());
                Threat saved = threatRepository.save(threat);

                for (LogEntry ev : evidence) {
                    if (ev.getId() != null) {
                        ThreatEvidence te = new ThreatEvidence();
                        te.setThreatId(saved.getId());
                        te.setLogEntryId(ev.getId());
                        te.setRelationshipType("CAUSATIVE_EVENT");
                        threatEvidenceRepository.save(te);
                    }
                }

                threatsCreated++;
                existingThreats.add(saved); // update local list to prevent duplicates within the same batch

                ThreatDto dto = toThreatDto(saved, evidence);
                newlyDetectedDtos.add(dto);
            }
        }

        String summaryMessage = String.format(
            "Detection complete: %d rules evaluated, %d threats detected, %d existing threat(s) unchanged.",
            rulesEvaluated, threatsCreated, existingUnchanged
        );

        log.info(summaryMessage);
        DetectionResultDto response = new DetectionResultDto(rulesEvaluated, threatsCreated, existingUnchanged, summaryMessage);
        response.setNewlyDetectedThreats(newlyDetectedDtos);
        return response;
    }

    private ThreatDto toThreatDto(Threat threat, List<LogEntry> evidence) {
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

        if (evidence != null) {
            List<LogEntryDto> evDtos = evidence.stream().map(e -> {
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
        }

        return dto;
    }
}