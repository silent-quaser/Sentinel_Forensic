# SentinelForensic – REST API Specification

**Base URL**: `http://localhost:8081/api`  
**Content-Type**: `application/json` (or `multipart/form-data` for file uploads, `application/pdf` for reports)

---

## 1. System & Health

### `GET /health`
Returns the operational health of the platform, database connectivity, loaded detection rules, and parser readiness.

**Response `200 OK`**:
```json
{
  "backendStatus": "Operational",
  "databaseStatus": "Connected",
  "detectionEngineStatus": "Active (7 Rules Loaded)",
  "parserStatus": "Available (SystemLogParser)",
  "applicationVersion": "1.0.0-RELEASE",
  "timestamp": "2026-09-19T16:39:22.6850959"
}
```

---

## 2. Dashboard Analytics

### `GET /dashboard/summary`
Returns aggregate statistics across all investigations, logs, and threats for the SOC command dashboard.

**Response `200 OK`**:
```json
{
  "totalInvestigations": 3,
  "activeInvestigations": 2,
  "totalLogsIngested": 1420,
  "totalThreatsDetected": 5,
  "criticalThreats": 1,
  "highThreats": 2,
  "mediumThreats": 2,
  "lowThreats": 0,
  "severityDistribution": {
    "CRITICAL": 1,
    "HIGH": 2,
    "MEDIUM": 2,
    "LOW": 0
  },
  "recentActivities": [
    {
      "id": 1,
      "caseNumber": "INC-2026-001",
      "action": "Threat Detected: BRUTE_FORCE_001",
      "timestamp": "2026-09-19T10:30:00Z"
    }
  ]
}
```

---

## 3. Investigations Management

### `GET /investigations`
Retrieves all forensic investigations. Supports filtering by status or severity.

**Query Parameters**:
- `status` *(optional)*: `OPEN`, `IN_PROGRESS`, `UNDER_REVIEW`, `CLOSED`, `ARCHIVED`
- `severity` *(optional)*: `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`

**Response `200 OK`**:
```json
[
  {
    "id": 1,
    "caseNumber": "INC-2026-001",
    "title": "Unauthorized SSH Infiltration & Lockout",
    "description": "Suspicious login attempts detected on corporate edge gateway.",
    "leadInvestigator": "Naveen (Investigator)",
    "status": "OPEN",
    "severity": "CRITICAL",
    "logCount": 10,
    "threatCount": 2,
    "createdAt": "2026-09-19T10:15:00Z",
    "updatedAt": "2026-09-19T11:00:00Z"
  }
]
```

### `POST /investigations`
Creates a new forensic investigation.

**Request Body**:
```json
{
  "title": "Database Server Tampering Incident",
  "description": "Unusual privilege escalation detected on core PostgreSQL node.",
  "leadInvestigator": "Naveen (Investigator)",
  "severity": "HIGH"
}
```

**Response `201 Created`**:
```json
{
  "id": 2,
  "caseNumber": "INC-2026-002",
  "title": "Database Server Tampering Incident",
  "description": "Unusual privilege escalation detected on core PostgreSQL node.",
  "leadInvestigator": "Naveen (Investigator)",
  "status": "OPEN",
  "severity": "HIGH",
  "logCount": 0,
  "threatCount": 0,
  "createdAt": "2026-09-19T16:45:00Z",
  "updatedAt": "2026-09-19T16:45:00Z"
}
```

### `GET /investigations/{id}`
Returns full details for an investigation including counts and status.

---

## 4. Forensic Log Ingestion

### `POST /investigations/{id}/logs`
Ingests forensic logs into an investigation. Accepts raw log text or multipart log files (`.log`, `.txt`, `.csv`).

**Request Body (Raw JSON)**:
```json
{
  "rawLogContent": "2026-09-19 09:12:00 [SYSTEM] AUTH_FAILED User 'admin' failed login from 192.168.1.105\n2026-09-19 09:13:00 [SYSTEM] AUTH_FAILED User 'admin' failed login from 192.168.1.105"
}
```

**Response `200 OK`**:
```json
{
  "investigationId": 1,
  "parsedCount": 2,
  "skippedCount": 0,
  "message": "Successfully ingested 2 log entries."
}
```

### `GET /investigations/{id}/logs`
Lists ingested logs for an investigation with search, filtering, and pagination.

**Query Parameters**:
- `query` *(optional)*: Search in message, source IP, or username
- `eventType` *(optional)*: `AUTH_FAILED`, `AUTH_SUCCESS`, `ACCOUNT_LOCKED`, `PRIV_ESCALATION`, etc.
- `page` *(optional, default 0)*
- `size` *(optional, default 50)*

---

## 5. Threat Detection Engine

### `POST /investigations/{id}/detect`
Runs the polymorphic detection engine across all ingested logs for the case.

**Response `200 OK`**:
```json
{
  "investigationId": 1,
  "evaluatedRulesCount": 7,
  "threatsDetected": 2,
  "newThreatsCreated": 2,
  "duplicateThreatsIgnored": 0,
  "threats": [
    {
      "id": 1,
      "ruleId": "BRUTE_FORCE_001",
      "ruleName": "Brute Force Authentication Attempt",
      "threatScore": 65,
      "severity": "CRITICAL",
      "overrideReason": "Account lock following confirmed brute-force activity",
      "status": "OPEN",
      "evidenceCount": 4,
      "detectedAt": "2026-09-19T11:05:00Z"
    },
    {
      "id": 2,
      "ruleId": "ACCOUNT_CREATE_001",
      "ruleName": "Contextual Rapid Account Creation",
      "threatScore": 40,
      "severity": "MEDIUM",
      "overrideReason": null,
      "status": "OPEN",
      "evidenceCount": 1,
      "detectedAt": "2026-09-19T11:05:00Z"
    }
  ]
}
```

### `PUT /threats/{threatId}/status`
Updates threat triage status.

**Request Body**:
```json
{
  "status": "REVIEWED"
}
```

### `GET /threats/{threatId}/evidence`
Retrieves exact `LogEntry` records linked to this threat via foreign key relationship.

---

## 6. Timeline Reconstruction & Case Notes

### `GET /investigations/{id}/timeline`
Returns an ordered chronological timeline interleaving logs and detected threat markers.

### `POST /investigations/{id}/notes`
Adds an investigator note with badge and timestamp.

**Request Body**:
```json
{
  "author": "Naveen (Investigator)",
  "badge": "SF-INV-019",
  "content": "Confirmed IP 192.168.1.105 originates from unauthorized external VPN subnet."
}
```

---

## 7. Report Generation

### `GET /investigations/{id}/report`
Generates an official PDF forensic triage report using Apache PDFBox 3.x.

**Response**:
- `Content-Type`: `application/pdf`
- `Content-Disposition`: `attachment; filename="SentinelForensic-Report-INC-2026-001.pdf"`
