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
public class PasswordChangeRule implements ThreatRuleEvaluator {

    public static final String RULE_CODE = "PASSWORD_CHANGE_001";
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Override
    public String getRuleCode() {
        return RULE_CODE;
    }

    @Override
    public String getRuleName() {
        return "Password Change After Suspicious Activity";
    }

    @Override
    public String getDescription() {
        return "Detects failed login attempts followed by a password change for the same user within a defined time window, indicating potential account recovery abuse or forced reset.";
    }

    @Override
    public ThreatSeverity getDefaultSeverity() {
        return ThreatSeverity.HIGH;
    }

    @Override
    public List<ThreatDetectionResult> evaluate(List<LogEntry> entries, ThreatRule config) {
        List<ThreatDetectionResult> results = new ArrayList<>();
        if (entries == null || entries.isEmpty()) return results;

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
                if ("PASSWORD_CHANGED".equalsIgnoreCase(current.getEventType())) {
                    // Check for preceding LOGIN_FAILED events for this user within windowSeconds
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

                    if (!precedingFailures.isEmpty()) {
                        List<LogEntry> evidence = new ArrayList<>(precedingFailures);
                        evidence.add(current);

                        int failureCount = precedingFailures.size();
                        int score = 45 + (5 * failureCount); // 50+ -> HIGH
                        ThreatSeverity severity = score >= 75 ? ThreatSeverity.CRITICAL : (score >= 50 ? ThreatSeverity.HIGH : ThreatSeverity.MEDIUM);

                        LogEntry firstFail = precedingFailures.get(0);
                        String explanation = String.format(
                            "User '%s' had a password changed at %s following %d failed login attempt(s) starting at %s (within %d minutes).",
                            username, current.getTimestamp().format(TIME_FMT),
                            failureCount, firstFail.getTimestamp().format(TIME_FMT),
                            windowSeconds / 60
                        );

                        String scoreBreakdown = String.format(
                            "Suspicious sequence base: +45, %d prior login failure(s): +%d. Total score: %d.",
                            failureCount, 5 * failureCount, score
                        );

                        Threat threat = new Threat();
                        threat.setInvestigationId(current.getInvestigationId());
                        threat.setRuleCode(RULE_CODE);
                        threat.setTitle("Password Change After Suspicious Activity");
                        threat.setDescription("Password change occurred immediately following authentication failures for user " + username + ".");
                        threat.setExplanation(explanation);
                        threat.setSeverity(severity);
                        threat.setScore(score);
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
