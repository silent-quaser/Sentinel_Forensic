package com.sentinelforensic.detector.rules;

import com.sentinelforensic.detector.ThreatDetectionResult;
import com.sentinelforensic.detector.ThreatRuleEvaluator;
import com.sentinelforensic.model.LogEntry;
import com.sentinelforensic.model.Threat;
import com.sentinelforensic.model.ThreatRule;
import com.sentinelforensic.model.ThreatSeverity;
import com.sentinelforensic.model.ThreatStatus;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class AuthSuccessAfterFailureRule implements ThreatRuleEvaluator {

    public static final String RULE_CODE = "AUTH_SUCCESS_AFTER_FAILURE_001";
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Override
    public String getRuleCode() {
        return RULE_CODE;
    }

    @Override
    public String getRuleName() {
        return "Successful Login After Repeated Failures";
    }

    @Override
    public String getDescription() {
        return "Detects multiple failed login attempts followed by a successful login for the same user within a defined time window, indicating potential credential compromise through guessing.";
    }

    @Override
    public ThreatSeverity getDefaultSeverity() {
        return ThreatSeverity.HIGH;
    }

    @Override
    public List<ThreatDetectionResult> evaluate(List<LogEntry> entries, ThreatRule config) {
        List<ThreatDetectionResult> results = new ArrayList<>();
        if (entries == null || entries.isEmpty()) return results;

        int threshold = (config != null && config.getThreshold() != null) ? config.getThreshold() : 2;
        int windowSeconds = (config != null && config.getTimeWindowSeconds() != null) ? config.getTimeWindowSeconds() : 900; // 15 mins

        Map<String, List<LogEntry>> userEntries = entries.stream()
                .filter(e -> e.getUsername() != null && !e.getUsername().isBlank())
                .collect(Collectors.groupingBy(LogEntry::getUsername));

        for (Map.Entry<String, List<LogEntry>> entry : userEntries.entrySet()) {
            String username = entry.getKey();
            List<LogEntry> logs = new ArrayList<>(entry.getValue());
            logs.sort(Comparator.comparing(LogEntry::getTimestamp).thenComparing(LogEntry::getId));

            for (int i = 0; i < logs.size(); i++) {
                LogEntry current = logs.get(i);
                if ("LOGIN_SUCCESS".equalsIgnoreCase(current.getEventType())) {
                    // Collect preceding LOGIN_FAILED within windowSeconds
                    List<LogEntry> precedingFailures = new ArrayList<>();
                    for (int j = 0; j < i; j++) {
                        LogEntry prior = logs.get(j);
                        if ("LOGIN_FAILED".equalsIgnoreCase(prior.getEventType())) {
                            long diff = Duration.between(prior.getTimestamp(), current.getTimestamp()).getSeconds();
                            if (diff <= windowSeconds && diff >= 0) {
                                precedingFailures.add(prior);
                            }
                        }
                    }

                    if (precedingFailures.size() >= threshold) {
                        List<LogEntry> evidence = new ArrayList<>(precedingFailures);
                        evidence.add(current);

                        int failureCount = precedingFailures.size();
                        int baseScore = 35;
                        int failureScore = 5 * failureCount;
                        int successBonus = 15;
                        int finalScore = baseScore + failureScore + successBonus;

                        ThreatSeverity severity = finalScore >= 75 ? ThreatSeverity.CRITICAL : (finalScore >= 50 ? ThreatSeverity.HIGH : ThreatSeverity.MEDIUM);

                        LogEntry firstFail = precedingFailures.get(0);
                        String explanation = String.format(
                            "User '%s' authenticated successfully at %s after %d failed login attempt(s) starting at %s within %d minutes.",
                            username, current.getTimestamp().format(TIME_FMT),
                            failureCount, firstFail.getTimestamp().format(TIME_FMT),
                            windowSeconds / 60
                        );

                        String scoreBreakdown = String.format(
                            "Base sequence rule: +%d, %d prior failures: +%d, Success after failures: +%d. Total score: %d.",
                            baseScore, failureCount, failureScore, successBonus, finalScore
                        );

                        Threat threat = new Threat();
                        threat.setInvestigationId(current.getInvestigationId());
                        threat.setRuleCode(RULE_CODE);
                        threat.setTitle("Successful Login After Repeated Failures");
                        threat.setDescription("Eventual successful authentication after repeated failed login attempts for user " + username + ".");
                        threat.setExplanation(explanation);
                        threat.setSeverity(severity);
                        threat.setScore(finalScore);
                        threat.setStatus(ThreatStatus.OPEN);
                        threat.setFirstObserved(firstFail.getTimestamp());
                        threat.setLastObserved(current.getTimestamp());
                        threat.setAffectedUser(username);
                        threat.setAffectedIp(firstFail.getIpAddress());
                        threat.setScoreBreakdown(scoreBreakdown);

                        results.add(new ThreatDetectionResult(threat, evidence));
                        break;
                    }
                }
            }
        }

        return results;
    }
}
