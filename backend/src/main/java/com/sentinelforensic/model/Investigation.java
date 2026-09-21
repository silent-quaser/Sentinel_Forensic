package com.sentinelforensic.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "investigations")
public class Investigation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String investigationId;
    
    @Column(nullable = false)
    private String name;
    
    private String investigatorName;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    private InvestigationStatus status;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        if (this.investigationId == null) {
            this.investigationId = UUID.randomUUID().toString();
        }
        if (this.status == null) {
            this.status = InvestigationStatus.OPEN;
        }
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }

    public String getInvestigationId() { return this.investigationId; }
    public void setInvestigationId(String investigationId) { this.investigationId = investigationId; }

    public String getName() { return this.name; }
    public void setName(String name) { this.name = name; }

    public String getInvestigatorName() { return this.investigatorName; }
    public void setInvestigatorName(String investigatorName) { this.investigatorName = investigatorName; }

    public String getDescription() { return this.description; }
    public void setDescription(String description) { this.description = description; }

    public InvestigationStatus getStatus() { return this.status; }
    public void setStatus(InvestigationStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return this.createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return this.updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

}