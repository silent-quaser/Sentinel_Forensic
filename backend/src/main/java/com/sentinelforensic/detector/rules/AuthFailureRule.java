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
public class AuthFailureRule implements ThreatRuleEvaluator {

    public static final String RULE_CODE = "AUTH_FAILURE_001";
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Override
    public String getRuleCode() {
        return RULE_CODE;
    }

    @Override
    public String getRuleName() {
        return "Repeated Authentication Failures";
    }

    @Override
    public String getDescription() {
        return "Detects 2 or more failed login events for the same user within a 30-minute window, identifying suspicious repeated authentication activity without duplicating brute-force detections.";
    }

    @Override
    public ThreatSeverity getDefaultSeverity() {
        return ThreatSeverity.MEDIUM;
    }

    @Override
    public List<ThreatDetectionResult> evaluate(List<LogEntry> entries, ThreatRule config) {
        List<ThreatDetectionResult> results = new ArrayList<>();
        if (entries == null || entries.isEmpty()) return results;

        int threshold = (config != null && config.getThreshold() != null) ? config.getThreshold() : 2;
        int windowSeconds = (config != null && config.getTimeWindowSeconds() != null) ? config.getTimeWindowSeconds() : 1800; // 30 mins

        Map<String, List<LogEntry>> userEntries = entries.stream()
                .filter(e -> e.getUsername() != null && !e.getUsername().isBlank())
                .collect(Collectors.groupingBy(LogEntry::getUsername));

        for (Map.Entry<String, List<LogEntry>> entry : userEntries.entrySet()) {
            String username = entry.getKey();
            List<LogEntry> logs = new ArrayList<>(entry.getValue());
            logs.sort(Comparator.comparing(LogEntry::getTimestamp).thenComparing(LogEntry::getId));

            List<LogEntry> failedLogins = logs.stream()
                    .filter(e -> "LOGIN_FAILED".equalsIgnoreCase(e.getEventType()))
                    .collect(Collectors.toList());

            if (failedLogins.size() < threshold) {
                continue;
            }

            // Group failed logins within windowSeconds
            List<LogEntry> matchedFailures = new ArrayList<>();
            for (int i = 0; i < failedLogins.size(); i++) {
                List<LogEntry> currentGroup = new ArrayList<>();
                currentGroup.add(failedLogins.get(i));

                for (int j = i + 1; j < failedLogins.size(); j++) {
                    long diff = Duration.between(failedLogins.get(i).getTimestamp(), failedLogins.get(j).getTimestamp()).getSeconds();
                    if (diff <= windowSeconds) {
                        currentGroup.add(failedLogins.get(j));
                    }
                }

                if (currentGroup.size() >= threshold) {
                    matchedFailures = currentGroup;
                    break;
                }
            }

            if (!matchedFailures.isEmpty()) {
                LogEntry first = matchedFailures.get(0);
                LogEntry last = matchedFailures.get(matchedFailures.size() - 1);
                int count = matchedFailures.size();

                // Base scoring: Base +20, +5 per attempt
                int score = 20 + (5 * count);
                ThreatSeverity severity = (config != null && config.getSeverity() != null) ? config.getSeverity() : ThreatSeverity.MEDIUM;

                String scoreBreakdown = String.format("Base auth-failure rule: +20, %d failed attempts: +%d. Total score: %d.", count, 5 * count, score);
                String explanation = String.format(
                    "Detected %d failed login attempts for user '%s' between %s and %s within a %d-minute window.",
                    count, username,
                    first.getTimestamp().format(TIME_FMT),
                    last.getTimestamp().format(TIME_FMT),
                    windowSeconds / 60
                );

                Threat threat = new Threat();
                threat.setInvestigationId(first.getInvestigationId());
                threat.setRuleCode(RULE_CODE);
                threat.setTitle("Repeated Authentication Failures");
                threat.setDescription("User " + username + " experienced repeated failed logins.");
                threat.setExplanation(explanation);
                threat.setSeverity(severity);
                threat.setScore(score);
                threat.setStatus(ThreatStatus.OPEN);
                threat.setFirstObserved(first.getTimestamp());
                threat.setLastObserved(last.getTimestamp());
                threat.setAffectedUser(username);
                threat.setAffectedIp(first.getIpAddress());
                threat.setScoreBreakdown(scoreBreakdown);

                results.add(new ThreatDetectionResult(threat, matchedFailures));
            }
        }

        return results;
    }
}
