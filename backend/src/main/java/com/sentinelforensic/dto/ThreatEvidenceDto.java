package com.sentinelforensic.dto;

public class ThreatEvidenceDto {
    private Long id;
    private Long threatId;
    private Long logEntryId;
    private String relationshipType;
    private LogEntryDto logEntry;

    public ThreatEvidenceDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getThreatId() { return threatId; }
    public void setThreatId(Long threatId) { this.threatId = threatId; }

    public Long getLogEntryId() { return logEntryId; }
    public void setLogEntryId(Long logEntryId) { this.logEntryId = logEntryId; }

    public String getRelationshipType() { return relationshipType; }
    public void setRelationshipType(String relationshipType) { this.relationshipType = relationshipType; }

    public LogEntryDto getLogEntry() { return logEntry; }
    public void setLogEntry(LogEntryDto logEntry) { this.logEntry = logEntry; }
}
