package com.sentinelforensic.detector;

import com.sentinelforensic.model.LogEntry;
import com.sentinelforensic.model.ThreatRule;
import com.sentinelforensic.model.ThreatSeverity;

import java.util.List;

public interface ThreatRuleEvaluator {
    String getRuleCode();
    String getRuleName();
    String getDescription();
    ThreatSeverity getDefaultSeverity();
    List<ThreatDetectionResult> evaluate(List<LogEntry> entries, ThreatRule config);
}