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
public class BruteForceRule implements ThreatRuleEvaluator {

    public static final String RULE_CODE = "BRUTE_FORCE_001";
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Override
    public String getRuleCode() {
        return RULE_CODE;
    }

    @Override
    public String getRuleName() {
        return "Brute Force Login Attempt";
    }

    @Override
    public String getDescription() {
        return "Detects multiple failed login attempts for the same user within a defined time window, escalating to CRITICAL if followed by account lockout.";
    }

    @Override
    public ThreatSeverity getDefaultSeverity() {
        return ThreatSeverity.HIGH;
    }

    @Override
    public List<ThreatDetectionResult> evaluate(List<LogEntry> entries, ThreatRule config) {
        List<ThreatDetectionResult> results = new ArrayList<>();
        if (entries == null || entries.isEmpty()) return results;

        int threshold = (config != null && config.getThreshold() != null) ? config.getThreshold() : 3;
        int windowSeconds = (config != null && config.getTimeWindowSeconds() != null) ? config.getTimeWindowSeconds() : 600;

        // Group entries by user
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

            // Sliding window to detect threshold breaches
            int n = failedLogins.size();
            for (int i = 0; i <= n - threshold; i++) {
                List<LogEntry> windowFailures = new ArrayList<>();
                windowFailures.add(failedLogins.get(i));

                for (int j = i + 1; j < n; j++) {
                    long diffSeconds = Duration.between(failedLogins.get(i).getTimestamp(), failedLogins.get(j).getTimestamp()).getSeconds();
                    if (diffSeconds <= windowSeconds) {
                        windowFailures.add(failedLogins.get(j));
                    }
                }

                if (windowFailures.size() >= threshold) {
                    LogEntry firstFail = windowFailures.get(0);
                    LogEntry lastFail = windowFailures.get(windowFailures.size() - 1);

                    // Check for subsequent ACCOUNT_LOCKED within 5 minutes (300 seconds) of the last failed attempt
                    Optional<LogEntry> accountLock = logs.stream()
                            .filter(e -> "ACCOUNT_LOCKED".equalsIgnoreCase(e.getEventType()))
                            .filter(e -> !e.getTimestamp().isBefore(lastFail.getTimestamp()))
                            .filter(e -> Duration.between(lastFail.getTimestamp(), e.getTimestamp()).getSeconds() <= 300)
                            .findFirst();

                    // Evidence events
                    List<LogEntry> evidence = new ArrayList<>(windowFailures);
                    boolean hasAccountLock = accountLock.isPresent();
                    accountLock.ifPresent(evidence::add);

                    // Calculate score: Score = 30 + (5 * failedAttempts) + accountLockBonus (+20)
                    int failedCount = windowFailures.size();
                    int baseScore = 30;
                    int failureScore = 5 * failedCount;
                    int lockBonus = hasAccountLock ? 20 : 0;
                    int finalScore = baseScore + failureScore + lockBonus;

                    // Standard severity for brute-force breach is HIGH (or CRITICAL if score >= 75)
                    ThreatSeverity severity = (finalScore >= 75) ? ThreatSeverity.CRITICAL : ThreatSeverity.HIGH;

                    // Mandatory Requirement 1: Explicit Escalation Override:
                    // When a confirmed brute-force sequence is followed by ACCOUNT_LOCKED within 5 minutes,
                    // explicitly override final severity to CRITICAL.
                    String escalationReason = null;
                    if (hasAccountLock) {
                        severity = ThreatSeverity.CRITICAL;
                        escalationReason = "Account lock following confirmed brute-force activity";
                    }

                    String scoreBreakdown = String.format(
                        "Base brute-force rule: +%d, %d failed attempts: +%d%s. Total calculated score: %d.",
                        baseScore, failedCount, failureScore,
                        (hasAccountLock ? ", Account lock: +20" : ""),
                        finalScore
                    );

                    String explanation = String.format(
                        "%d failed login attempts for user '%s' occurred between %s and %s within the %d-minute window%s.",
                        failedCount, username,
                        firstFail.getTimestamp().format(TIME_FMT),
                        lastFail.getTimestamp().format(TIME_FMT),
                        windowSeconds / 60,
                        (hasAccountLock ? ", followed by an account lock at " + accountLock.get().getTimestamp().format(TIME_FMT) : "")
                    );

                    Threat threat = new Threat();
                    threat.setInvestigationId(firstFail.getInvestigationId());
                    threat.setRuleCode(RULE_CODE);
                    threat.setTitle("Brute Force Login Attempt");
                    threat.setDescription("Multiple consecutive authentication failures detected for user " + username + ".");
                    threat.setExplanation(explanation);
                    threat.setSeverity(severity);
                    threat.setScore(finalScore);
                    threat.setStatus(ThreatStatus.OPEN);
                    threat.setFirstObserved(firstFail.getTimestamp());
                    threat.setLastObserved(hasAccountLock ? accountLock.get().getTimestamp() : lastFail.getTimestamp());
                    threat.setAffectedUser(username);
                    threat.setAffectedIp(firstFail.getIpAddress());
                    threat.setEscalationReason(escalationReason);
                    threat.setScoreBreakdown(scoreBreakdown);

                    results.add(new ThreatDetectionResult(threat, evidence));
                    break; // One consolidated brute force threat per user sequence
                }
            }
        }

        return results;
    }
}