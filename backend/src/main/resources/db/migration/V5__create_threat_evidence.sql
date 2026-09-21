CREATE TABLE threat_evidence (
    id BIGSERIAL PRIMARY KEY,
    threat_id BIGINT NOT NULL REFERENCES threats(id) ON DELETE CASCADE,
    log_entry_id BIGINT NOT NULL REFERENCES log_entries(id) ON DELETE CASCADE,
    relationship_type VARCHAR(100) DEFAULT 'CAUSATIVE_EVENT'
);

CREATE INDEX idx_threat_evidence_threat_id ON threat_evidence(threat_id);
CREATE INDEX idx_threat_evidence_log_entry_id ON threat_evidence(log_entry_id);
