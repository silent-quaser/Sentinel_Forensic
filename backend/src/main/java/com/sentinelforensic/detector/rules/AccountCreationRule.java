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
public class AccountCreationRule implements ThreatRuleEvaluator {

    public static final String RULE_CODE = "ACCOUNT_CREATE_001";
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Override
    public String getRuleCode() {
        return RULE_CODE;
    }

    @Override
    public String getRuleName() {
        return "Unexpected Account Creation";
    }

    @Override
    public String getDescription() {
        return "Identifies account creation requiring investigation based on contextual indicators: rapid account creation or creation following suspicious authentication failures.";
    }

    @Override
    public ThreatSeverity getDefaultSeverity() {
        return ThreatSeverity.MEDIUM;
    }

    @Override
    public List<ThreatDetectionResult> evaluate(List<LogEntry> entries, ThreatRule config) {
        List<ThreatDetectionResult> results = new ArrayList<>();
        if (entries == null || entries.isEmpty()) return results;

        int windowSeconds = (config != null && config.getTimeWindowSeconds() != null) ? config.getTimeWindowSeconds() : 600; // 10 mins

        List<LogEntry> sorted = new ArrayList<>(entries);
        sorted.sort(Comparator.comparing(LogEntry::getTimestamp).thenComparing(LogEntry::getId));

        List<LogEntry> userCreatedEvents = sorted.stream()
                .filter(e -> "USER_CREATED".equalsIgnoreCase(e.getEventType()))
                .collect(Collectors.toList());

        if (userCreatedEvents.isEmpty()) return results;

        // Contextual Check 1: Multiple USER_CREATED events within short window (>= 2 within windowSeconds)
        if (userCreatedEvents.size() >= 2) {
            for (int i = 0; i < userCreatedEvents.size() - 1; i++) {
                LogEntry first = userCreatedEvents.get(i);
                LogEntry second = userCreatedEvents.get(i + 1);
                long diff = Duration.between(first.getTimestamp(), second.getTimestamp()).getSeconds();
                if (diff <= windowSeconds) {
                    List<LogEntry> evidence = Arrays.asList(first, second);
                    int score = 45;
                    Threat threat = new Threat();
                    threat.setInvestigationId(first.getInvestigationId());
                    threat.setRuleCode(RULE_CODE);
                    threat.setTitle("Rapid Account Creation Activity");
                    threat.setDescription("Multiple accounts were created in rapid succession.");
                    threat.setExplanation(String.format(
                        "Multiple user accounts ('%s' at %s, '%s' at %s) were created within %d seconds of each other.",
                        first.getUsername(), first.getTimestamp().format(TIME_FMT),
                        second.getUsername(), second.getTimestamp().format(TIME_FMT),
                        diff
                    ));
                    threat.setSeverity(ThreatSeverity.MEDIUM);
                    threat.setScore(score);
                    threat.setStatus(ThreatStatus.OPEN);
                    threat.setFirstObserved(first.getTimestamp());
                    threat.setLastObserved(second.getTimestamp());
                    threat.setAffectedUser(second.getUsername());
                    threat.setScoreBreakdown("Rapid account creation: +35, Window proximity: +10. Total score: 45.");
                    results.add(new ThreatDetectionResult(threat, evidence));
                    break;
                }
            }
        }

        // Contextual Check 2: Account creation following suspicious authentication failures (within 15 minutes)
        for (LogEntry created : userCreatedEvents) {
            List<LogEntry> priorFailures = sorted.stream()
                    .filter(e -> "LOGIN_FAILED".equalsIgnoreCase(e.getEventType()))
                    .filter(e -> e.getTimestamp().isBefore(created.getTimestamp()))
                    .filter(e -> Duration.between(e.getTimestamp(), created.getTimestamp()).getSeconds() <= 900)
                    .collect(Collectors.toList());

            if (priorFailures.size() >= 2) {
                List<LogEntry> evidence = new ArrayList<>(priorFailures);
                evidence.add(created);

                int score = 40;
                Threat threat = new Threat();
                threat.setInvestigationId(created.getInvestigationId());
                threat.setRuleCode(RULE_CODE);
                threat.setTitle("Account Created Following Authentication Failures");
                threat.setDescription("New account created shortly after repeated login failures.");
                threat.setExplanation(String.format(
                    "User account '%s' was created at %s, shortly following %d failed login attempts in the preceding 15 minutes.",
                    created.getUsername(), created.getTimestamp().format(TIME_FMT), priorFailures.size()
                ));
                threat.setSeverity(ThreatSeverity.MEDIUM);
                threat.setScore(score);
                threat.setStatus(ThreatStatus.OPEN);
                threat.setFirstObserved(priorFailures.get(0).getTimestamp());
                threat.setLastObserved(created.getTimestamp());
                threat.setAffectedUser(created.getUsername());
                threat.setScoreBreakdown(String.format("Account creation: +20, %d preceding failures: +20. Total score: %d.", priorFailures.size(), score));
                results.add(new ThreatDetectionResult(threat, evidence));
                break;
            }
        }

        return results;
    }
}
