# SentinelForensic Database Architecture & Schema Documentation

SentinelForensic utilizes **PostgreSQL 18** with **Flyway** for deterministic, repeatable schema versioning.

---

## Entity Relationship Overview

```
+-------------------+       1:N       +-------------------+
|  investigations   |<--------------->|    log_entries    |
+-------------------+                 +-------------------+
        | 1                                     | 1
        |                                       |
        | 1:N                                   | 1:N
        v                                       v
+-------------------+       1:N       +-------------------+
|      threats      |<--------------->|  threat_evidence  |
+-------------------+                 +-------------------+
        ^
        | References
+-------------------+
|   threat_rules    |
+-------------------+
```

---

## Table Definitions

### 1. `investigations`
Primary case dossiers managed by forensic investigators.
- `id` (BIGSERIAL, PK)
- `investigation_id` (VARCHAR(50), UNIQUE, NOT NULL) — e.g. `INV-2026-001`
- `name` (VARCHAR(255), NOT NULL)
- `investigator_name` (VARCHAR(255), NOT NULL)
- `description` (TEXT)
- `status` (VARCHAR(50), NOT NULL, DEFAULT 'OPEN') — `OPEN`, `IN_PROGRESS`, `CLOSED`, `ARCHIVED`
- `created_at` (TIMESTAMP, NOT NULL, DEFAULT CURRENT_TIMESTAMP)
- `updated_at` (TIMESTAMP, NOT NULL, DEFAULT CURRENT_TIMESTAMP)

### 2. `log_entries`
Normalized forensic audit log events ingested from external files.
- `id` (BIGSERIAL, PK)
- `investigation_id` (BIGINT, FK -> `investigations(id)` ON DELETE CASCADE)
- `timestamp` (TIMESTAMP, NOT NULL)
- `event_type` (VARCHAR(100), NOT NULL) — e.g. `LOGIN_FAILED`, `LOGIN_SUCCESS`, `ACCOUNT_LOCKED`
- `username` (VARCHAR(255))
- `source` (VARCHAR(255))
- `source_type` (VARCHAR(50))
- `ip_address` (VARCHAR(45))
- `hostname` (VARCHAR(255))
- `process_name` (VARCHAR(255))
- `process_id` (INTEGER)
- `severity` (VARCHAR(50), NOT NULL)
- `description` (TEXT)
- `raw_message` (TEXT, NOT NULL) — Preserves complete verbatim record
- `metadata` (JSONB)
- `created_at` (TIMESTAMP, NOT NULL, DEFAULT CURRENT_TIMESTAMP)

### 3. `threat_rules`
Catalog of forensic correlation rules and sliding window configurations.
- `id` (BIGSERIAL, PK)
- `rule_code` (VARCHAR(100), UNIQUE, NOT NULL) — e.g. `BRUTE_FORCE_001`
- `name` (VARCHAR(255), NOT NULL)
- `description` (TEXT NOT NULL)
- `severity` (VARCHAR(50), NOT NULL)
- `threshold_count` (INT NOT NULL)
- `time_window_seconds` (INT NOT NULL)
- `time_window_minutes` (INT NOT NULL)
- `enabled` (BOOLEAN NOT NULL DEFAULT TRUE)
- `created_at` (TIMESTAMP, NOT NULL, DEFAULT CURRENT_TIMESTAMP)

### 4. `threats`
Correlated threat incidents identified across log events.
- `id` (BIGSERIAL, PK)
- `investigation_id` (BIGINT, FK -> `investigations(id)` ON DELETE CASCADE)
- `rule_code` (VARCHAR(100), NOT NULL)
- `title` (VARCHAR(255), NOT NULL)
- `description` (TEXT NOT NULL)
- `explanation` (TEXT NOT NULL)
- `severity` (VARCHAR(50), NOT NULL) — `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`
- `score` (INT NOT NULL) — Heuristic Detection Score (0–100)
- `status` (VARCHAR(50), NOT NULL DEFAULT 'OPEN') — `OPEN`, `DETECTED`, `REVIEWED`, `CONFIRMED`, `FALSE_POSITIVE`, `RESOLVED`
- `first_observed` (TIMESTAMP)
- `last_observed` (TIMESTAMP)
- `detected_at` (TIMESTAMP, NOT NULL DEFAULT CURRENT_TIMESTAMP)
- `affected_user` (VARCHAR(255))
- `affected_ip` (VARCHAR(100))
- `escalation_reason` (TEXT) — Explicit rationale when severity overrides score threshold
- `score_breakdown` (TEXT) — Transparent formula components

### 5. `threat_evidence`
Join table linking each detected threat directly to its exact causative `log_entries`.
- `id` (BIGSERIAL, PK)
- `threat_id` (BIGINT, FK -> `threats(id)` ON DELETE CASCADE)
- `log_entry_id` (BIGINT, FK -> `log_entries(id)` ON DELETE CASCADE)
- `evidence_role` (VARCHAR(100), NOT NULL)
- `created_at` (TIMESTAMP, NOT NULL DEFAULT CURRENT_TIMESTAMP)

### 6. `investigation_notes`
Investigator commentary and formal triage observations.
- `id` (BIGSERIAL, PK)
- `investigation_id` (BIGINT, FK -> `investigations(id)` ON DELETE CASCADE)
- `author` (VARCHAR(255), NOT NULL)
- `content` (TEXT NOT NULL)
- `created_at` (TIMESTAMP, NOT NULL DEFAULT CURRENT_TIMESTAMP)

---

## Flyway Migration History
1. `V1__create_investigations.sql`
2. `V2__create_log_entries.sql`
3. `V3__create_threat_rules.sql`
4. `V4__create_threats.sql`
5. `V5__create_threat_evidence.sql`
6. `V6__create_investigation_notes.sql`
