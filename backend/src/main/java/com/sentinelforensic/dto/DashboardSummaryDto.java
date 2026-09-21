package com.sentinelforensic.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardSummaryDto {
    private long activeInvestigations;
    private long totalEvents;
    private long detectedThreats;
    private long highCriticalThreats;
    private long distinctUsers;

    private Map<String, Long> threatSeverityDistribution = new HashMap<>();
    private Map<String, Long> eventTypeDistribution = new HashMap<>();
    private List<InvestigationResponse> recentInvestigations = new ArrayList<>();
    private List<ThreatDto> recentThreats = new ArrayList<>();
    private List<ActivityPointDto> activitySeries = new ArrayList<>();

    public DashboardSummaryDto() {}

    public long getActiveInvestigations() { return activeInvestigations; }
    public void setActiveInvestigations(long activeInvestigations) { this.activeInvestigations = activeInvestigations; }

    public long getTotalEvents() { return totalEvents; }
    public void setTotalEvents(long totalEvents) { this.totalEvents = totalEvents; }

    public long getDetectedThreats() { return detectedThreats; }
    public void setDetectedThreats(long detectedThreats) { this.detectedThreats = detectedThreats; }

    public long getHighCriticalThreats() { return highCriticalThreats; }
    public void setHighCriticalThreats(long highCriticalThreats) { this.highCriticalThreats = highCriticalThreats; }

    public long getDistinctUsers() { return distinctUsers; }
    public void setDistinctUsers(long distinctUsers) { this.distinctUsers = distinctUsers; }

    public Map<String, Long> getThreatSeverityDistribution() { return threatSeverityDistribution; }
    public void setThreatSeverityDistribution(Map<String, Long> threatSeverityDistribution) { this.threatSeverityDistribution = threatSeverityDistribution; }

    public Map<String, Long> getEventTypeDistribution() { return eventTypeDistribution; }
    public void setEventTypeDistribution(Map<String, Long> eventTypeDistribution) { this.eventTypeDistribution = eventTypeDistribution; }

    public List<InvestigationResponse> getRecentInvestigations() { return recentInvestigations; }
    public void setRecentInvestigations(List<InvestigationResponse> recentInvestigations) { this.recentInvestigations = recentInvestigations; }

    public List<ThreatDto> getRecentThreats() { return recentThreats; }
    public void setRecentThreats(List<ThreatDto> recentThreats) { this.recentThreats = recentThreats; }

    public List<ActivityPointDto> getActivitySeries() { return activitySeries; }
    public void setActivitySeries(List<ActivityPointDto> activitySeries) { this.activitySeries = activitySeries; }
}
