package com.sentinelforensic.model;

import jakarta.persistence.*;

@Entity
@Table(name = "threat_evidence")
public class ThreatEvidence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long threatId;
    
    @Column(nullable = false)
    private Long logEntryId;
    
    private String relationshipType;

    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }

    public Long getThreatId() { return this.threatId; }
    public void setThreatId(Long threatId) { this.threatId = threatId; }

    public Long getLogEntryId() { return this.logEntryId; }
    public void setLogEntryId(Long logEntryId) { this.logEntryId = logEntryId; }

    public String getRelationshipType() { return this.relationshipType; }
    public void setRelationshipType(String relationshipType) { this.relationshipType = relationshipType; }

}