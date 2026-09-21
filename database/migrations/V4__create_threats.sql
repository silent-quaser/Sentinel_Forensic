CREATE TABLE threats (
    id BIGSERIAL PRIMARY KEY,
    investigation_id BIGINT NOT NULL REFERENCES investigations(id) ON DELETE CASCADE,
    rule_code VARCHAR(100) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    explanation TEXT NOT NULL,
    severity VARCHAR(50) NOT NULL,
    score INT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'OPEN',
    first_observed TIMESTAMP,
    last_observed TIMESTAMP,
    detected_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    affected_user VARCHAR(255),
    affected_ip VARCHAR(100),
    escalation_reason TEXT,
    score_breakdown TEXT
);

CREATE INDEX idx_threats_investigation_id ON threats(investigation_id);
CREATE INDEX idx_threats_severity ON threats(severity);
CREATE INDEX idx_threats_status ON threats(status);
CREATE INDEX idx_threats_detected_at ON threats(detected_at);
CREATE INDEX idx_threats_rule_code ON threats(rule_code);
