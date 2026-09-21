package com.sentinelforensic.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LogEntryDto {
    private Long id;
    private Long investigationId;
    private LocalDateTime timestamp;
    private String eventType;
    private String username;
    private String source;
    private String sourceType;
    private String ipAddress;
    private String hostname;
    private String processName;
    private String processId;
    private String severity;
    private String description;
    private String rawMessage;
    private List<Long> referencedThreatIds = new ArrayList<>();

    public LogEntryDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getInvestigationId() { return investigationId; }
    public void setInvestigationId(Long investigationId) { this.investigationId = investigationId; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getHostname() { return hostname; }
    public void setHostname(String hostname) { this.hostname = hostname; }

    public String getProcessName() { return processName; }
    public void setProcessName(String processName) { this.processName = processName; }

    public String getProcessId() { return processId; }
    public void setProcessId(String processId) { this.processId = processId; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getRawMessage() { return rawMessage; }
    public void setRawMessage(String rawMessage) { this.rawMessage = rawMessage; }

    public List<Long> getReferencedThreatIds() { return referencedThreatIds; }
    public void setReferencedThreatIds(List<Long> referencedThreatIds) { this.referencedThreatIds = referencedThreatIds; }
}
