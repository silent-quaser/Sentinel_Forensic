CREATE TABLE investigation_notes (
    id BIGSERIAL PRIMARY KEY,
    investigation_id BIGINT NOT NULL REFERENCES investigations(id) ON DELETE CASCADE,
    author VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_investigation_notes_inv_id ON investigation_notes(investigation_id);
CREATE INDEX idx_investigation_notes_created_at ON investigation_notes(created_at);
