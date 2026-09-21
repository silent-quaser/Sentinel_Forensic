package com.sentinelforensic.dto;

public class ActivityPointDto {
    private String timestamp;
    private long eventCount;
    private long threatCount;

    public ActivityPointDto() {}

    public ActivityPointDto(String timestamp, long eventCount, long threatCount) {
        this.timestamp = timestamp;
        this.eventCount = eventCount;
        this.threatCount = threatCount;
    }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public long getEventCount() { return eventCount; }
    public void setEventCount(long eventCount) { this.eventCount = eventCount; }

    public long getThreatCount() { return threatCount; }
    public void setThreatCount(long threatCount) { this.threatCount = threatCount; }
}
