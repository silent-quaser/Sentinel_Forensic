package com.sentinelforensic.dto;

import com.sentinelforensic.model.ThreatStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateThreatStatusRequest {
    @NotNull(message = "Threat status is required")
    private ThreatStatus status;

    public UpdateThreatStatusRequest() {}

    public UpdateThreatStatusRequest(ThreatStatus status) {
        this.status = status;
    }

    public ThreatStatus getStatus() { return status; }
    public void setStatus(ThreatStatus status) { this.status = status; }
}
