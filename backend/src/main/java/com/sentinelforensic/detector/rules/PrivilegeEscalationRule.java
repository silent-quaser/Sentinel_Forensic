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
public class PrivilegeEscalationRule implements ThreatRuleEvaluator {

    public static final String RULE_CODE = "PRIV_ESC_001";
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private static final Set<String> PRIV_EVENT_TYPES = Set.of(
        "ROLE_CHANGED", "PRIVILEGE_GRANTED", "ADMIN_ASSIGNED", "ELEVATED_PRIVILEGE"
    );

    @Override
    public String getRuleCode() {
        return RULE_CODE;
    }

    @Override
    public String getRuleName() {
        return "Potential Privilege Escalation";
    }

    @Override
    public String getDescription() {
        return "Detects privilege modification events occurring within suspicious context (e.g. immediately following failed authentication attempts). Does not generate threats for ordinary privilege actions without contextual evidence.";
    }

    @Override
    public ThreatSeverity getDefaultSeverity() {
        return ThreatSeverity.HIGH;
    }

    @Override
    public List<ThreatDetectionResult> evaluate(List<LogEntry> entries, ThreatRule config) {
        List<ThreatDetectionResult> results = new ArrayList<>();
        if (entries == null || entries.isEmpty()) return results;

        int windowSeconds = (config != null && config.getTimeWindowSeconds() != null) ? config.getTimeWindowSeconds() : 300; // 5 mins

        List<LogEntry> sorted = new ArrayList<>(entries);
        sorted.sort(Comparator.comparing(LogEntry::getTimestamp).thenComparing(LogEntry::getId));

        List<LogEntry> privEvents = sorted.stream()
                .filter(e -> e.getEventType() != null && PRIV_EVENT_TYPES.contains(e.getEventType().toUpperCase()))
                .collect(Collectors.toList());

        if (privEvents.isEmpty()) {
            return results; // Return no detection when no privilege events exist
        }

        // Only generate threat if there is contextual evidence of suspicious activity:
        // E.g., privilege escalation occurred within 5 minutes following repeated failed logins for that user or actor
        for (LogEntry privEvent : privEvents) {
            String user = privEvent.getUsername();
            List<LogEntry> relatedPriorFailures = sorted.stream()
                    .filter(e -> "LOGIN_FAILED".equalsIgnoreCase(e.getEventType()))
                    .filter(e -> e.getUsername() != null && e.getUsername().equalsIgnoreCase(user))
                    .filter(e -> e.getTimestamp().isBefore(privEvent.getTimestamp()))
                    .filter(e -> Duration.between(e.getTimestamp(), privEvent.getTimestamp()).getSeconds() <= windowSeconds)
                    .collect(Collectors.toList());

            if (!relatedPriorFailures.isEmpty()) {
                List<LogEntry> evidence = new ArrayList<>(relatedPriorFailures);
                evidence.add(privEvent);

                int score = 60;
                ThreatSeverity severity = ThreatSeverity.HIGH;

                String explanation = String.format(
                    "Privilege modification event '%s' for user '%s' at %s followed %d failed authentication attempt(s) within %d minutes.",
                    privEvent.getEventType(), user, privEvent.getTimestamp().format(TIME_FMT),
                    relatedPriorFailures.size(), windowSeconds / 60
                );

                String scoreBreakdown = String.format(
                    "Privilege change baseline: +40, %d prior login failure(s): +%d. Total score: %d.",
                    relatedPriorFailures.size(), 20, score
                );

                Threat threat = new Threat();
                threat.setInvestigationId(privEvent.getInvestigationId());
                threat.setRuleCode(RULE_CODE);
                threat.setTitle("Suspicious Privilege Modification");
                threat.setDescription("Privilege event observed immediately following authentication failures.");
                threat.setExplanation(explanation);
                threat.setSeverity(severity);
                threat.setScore(score);
                threat.setStatus(ThreatStatus.OPEN);
                threat.setFirstObserved(relatedPriorFailures.get(0).getTimestamp());
                threat.setLastObserved(privEvent.getTimestamp());
                threat.setAffectedUser(user);
                threat.setAffectedIp(privEvent.getIpAddress());
                threat.setScoreBreakdown(scoreBreakdown);

                results.add(new ThreatDetectionResult(threat, evidence));
            }
        }

        return results;
    }
}
