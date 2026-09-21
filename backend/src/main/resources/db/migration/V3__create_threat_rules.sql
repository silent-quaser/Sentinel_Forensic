CREATE TABLE threat_rules (
    id BIGSERIAL PRIMARY KEY,
    rule_code VARCHAR(100) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    severity VARCHAR(50) NOT NULL,
    threshold INT NOT NULL DEFAULT 1,
    time_window_seconds INT NOT NULL DEFAULT 600,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_threat_rules_code ON threat_rules(rule_code);

-- Seed Default 7 Threat Rules
INSERT INTO threat_rules (rule_code, name, description, severity, threshold, time_window_seconds, enabled)
VALUES
    ('BRUTE_FORCE_001', 'Brute Force Login Attempt', 'Detects multiple failed login attempts for the same user within a sliding time window, escalating to CRITICAL if followed by account lockout.', 'HIGH', 3, 600, TRUE),
    ('AUTH_FAILURE_001', 'Repeated Authentication Failures', 'Detects multiple authentication failures for a user within an extended window, identifying suspicious activity not meeting full brute-force criteria.', 'MEDIUM', 2, 1800, TRUE),
    ('ACCOUNT_CREATE_001', 'Unexpected Account Creation', 'Identifies suspicious account creation activity based on rapid succession or preceding authentication failures.', 'MEDIUM', 2, 600, TRUE),
    ('PASSWORD_CHANGE_001', 'Password Change After Suspicious Activity', 'Detects failed login attempts followed by a password change for the same user within a short time window.', 'HIGH', 1, 900, TRUE),
    ('AUTH_SUCCESS_AFTER_FAILURE_001', 'Successful Login After Repeated Failures', 'Detects multiple failed login attempts followed by a successful login for the same user, indicating potential credential compromise.', 'HIGH', 2, 900, TRUE),
    ('EVENT_BURST_001', 'Abnormal Event Burst', 'Detects unusually high event volume within a short duration, indicating high event concentration or potential scanning.', 'LOW', 10, 120, TRUE),
    ('PRIV_ESC_001', 'Potential Privilege Escalation', 'Detects privilege escalation events occurring within suspicious contextual activity.', 'HIGH', 1, 300, TRUE);
