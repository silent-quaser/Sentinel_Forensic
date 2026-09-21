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

@Component
public class EventBurstRule implements ThreatRuleEvaluator {

    public static final String RULE_CODE = "EVENT_BURST_001";
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Override
    public String getRuleCode() {
        return RULE_CODE;
    }

    @Override
    public String getRuleName() {
        return "Abnormal Event Burst";
    }

    @Override
    public String getDescription() {
        return "Detects unusually high event frequency within a short time window, indicating rapid automated activity, denial-of-service attempts, or log flooding.";
    }

    @Override
    public ThreatSeverity getDefaultSeverity() {
        return ThreatSeverity.LOW;
    }

    @Override
    public List<ThreatDetectionResult> evaluate(List<LogEntry> entries, ThreatRule config) {
        List<ThreatDetectionResult> results = new ArrayList<>();
        if (entries == null || entries.size() < 2) return results;

        int threshold = (config != null && config.getThreshold() != null) ? config.getThreshold() : 10;
        int windowSeconds = (config != null && config.getTimeWindowSeconds() != null) ? config.getTimeWindowSeconds() : 120; // 2 mins

        List<LogEntry> sorted = new ArrayList<>(entries);
        sorted.sort(Comparator.comparing(LogEntry::getTimestamp).thenComparing(LogEntry::getId));

        int n = sorted.size();
        for (int i = 0; i <= n - threshold; i++) {
            List<LogEntry> windowEvents = new ArrayList<>();
            windowEvents.add(sorted.get(i));

            for (int j = i + 1; j < n; j++) {
                long diff = Duration.between(sorted.get(i).getTimestamp(), sorted.get(j).getTimestamp()).getSeconds();
                if (diff <= windowSeconds) {
                    windowEvents.add(sorted.get(j));
                }
            }

            if (windowEvents.size() >= threshold) {
                LogEntry first = windowEvents.get(0);
                LogEntry last = windowEvents.get(windowEvents.size() - 1);
                int count = windowEvents.size();

                int score = Math.min(20 + count, 45); // LOW to MEDIUM
                ThreatSeverity severity = score >= 35 ? ThreatSeverity.MEDIUM : ThreatSeverity.LOW;

                String explanation = String.format(
                    "High event concentration detected: %d events occurred between %s and %s within %d seconds.",
                    count, first.getTimestamp().format(TIME_FMT), last.getTimestamp().format(TIME_FMT),
                    Math.max(Duration.between(first.getTimestamp(), last.getTimestamp()).getSeconds(), 1)
                );

                String scoreBreakdown = String.format("Event density baseline: +20, %d concentrated events: +%d. Total score: %d.", count, score - 20, score);

                Threat threat = new Threat();
                threat.setInvestigationId(first.getInvestigationId());
                threat.setRuleCode(RULE_CODE);
                threat.setTitle("Abnormal Event Burst");
                threat.setDescription("High concentration of log events in a brief interval.");
                threat.setExplanation(explanation);
                threat.setSeverity(severity);
                threat.setScore(score);
                threat.setStatus(ThreatStatus.OPEN);
                threat.setFirstObserved(first.getTimestamp());
                threat.setLastObserved(last.getTimestamp());
                threat.setAffectedUser("Multiple / System");
                threat.setScoreBreakdown(scoreBreakdown);

                results.add(new ThreatDetectionResult(threat, windowEvents));
                break; // One burst threat per evaluation
            }
        }

        return results;
    }
}
