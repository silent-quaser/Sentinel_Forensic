package com.sentinelforensic.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TimelineEventDto {
    private Long id;
    private Long investigationId;
    private LocalDateTime timestamp;
    private String eventType;
    private String username;
    private String source;
    private String severity;
    private String description;
    private String rawMessage;
    private boolean suspicious;
    private List<String> associatedThreatTitles = new ArrayList<>();
    private List<Long> associatedThreatIds = new ArrayList<>();

    public TimelineEventDto() {}

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

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getRawMessage() { return rawMessage; }
    public void setRawMessage(String rawMessage) { this.rawMessage = rawMessage; }

    public boolean isSuspicious() { return suspicious; }
    public void setSuspicious(boolean suspicious) { this.suspicious = suspicious; }

    public List<String> getAssociatedThreatTitles() { return associatedThreatTitles; }
    public void setAssociatedThreatTitles(List<String> associatedThreatTitles) { this.associatedThreatTitles = associatedThreatTitles; }

    public List<Long> getAssociatedThreatIds() { return associatedThreatIds; }
    public void setAssociatedThreatIds(List<Long> associatedThreatIds) { this.associatedThreatIds = associatedThreatIds; }
}
