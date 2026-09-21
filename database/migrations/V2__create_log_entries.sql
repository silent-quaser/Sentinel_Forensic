CREATE TABLE log_entries (
    id BIGSERIAL PRIMARY KEY,
    investigation_id BIGINT NOT NULL REFERENCES investigations(id) ON DELETE CASCADE,
    timestamp TIMESTAMP NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    username VARCHAR(255),
    source VARCHAR(255) DEFAULT 'System',
    source_type VARCHAR(255) DEFAULT 'SecurityLog',
    ip_address VARCHAR(100),
    hostname VARCHAR(255),
    process_name VARCHAR(255),
    process_id VARCHAR(50),
    severity VARCHAR(50) DEFAULT 'INFO',
    description TEXT,
    raw_message TEXT NOT NULL
);

CREATE INDEX idx_log_entries_investigation_id ON log_entries(investigation_id);
CREATE INDEX idx_log_entries_timestamp ON log_entries(timestamp);
CREATE INDEX idx_log_entries_username ON log_entries(username);
CREATE INDEX idx_log_entries_event_type ON log_entries(event_type);
