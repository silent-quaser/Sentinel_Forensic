package com.sentinelforensic.detector;

import com.sentinelforensic.model.LogEntry;
import com.sentinelforensic.model.Threat;
import java.util.ArrayList;
import java.util.List;

public class ThreatDetectionResult {
    private final Threat threat;
    private final List<LogEntry> evidenceEvents;

    public ThreatDetectionResult(Threat threat, List<LogEntry> evidenceEvents) {
        this.threat = threat;
        this.evidenceEvents = evidenceEvents != null ? evidenceEvents : new ArrayList<>();
    }

    public Threat getThreat() { return threat; }
    public List<LogEntry> getEvidenceEvents() { return evidenceEvents; }
}
