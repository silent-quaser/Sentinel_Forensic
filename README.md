# SentinelForensic
### Digital Incident Triage & Forensic Timeline Analysis Platform

[![Java 21](https://img.shields.io/badge/Java-21%20LTS-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-18-blue.svg)](https://www.postgresql.org/)
[![React](https://img.shields.io/badge/React-19.0-61dafb.svg)](https://react.dev/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.x-blue.svg)](https://www.typescriptlang.org/)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind-v4-38b2ac.svg)](https://tailwindcss.com/)

---

## 1. Project Overview

**SentinelForensic** is an enterprise-grade digital incident triage and forensic timeline reconstruction platform. It equips cybersecurity incident response teams, SOC tier-1/tier-2 analysts, and digital forensic examiners with an automated, rule-based triage system for rapid cyber incident analysis.

The system automates the processing of multi-source forensic logs, detects advanced adversary tactics via a polymorphic heuristic detection engine, maintains an immutable chain of custody with SHA-256 evidence hashing, reconstructs chronological forensic timelines, and generates formal incident reports via Apache PDFBox.

---

## 2. Key Capabilities & Features

1. **High-Performance Log Ingestion Engine**
   - Ingests structured and unstructured forensic logs (Syslog, Linux Auth, Windows Security).
   - Regex-based high-throughput parsing with timestamp normalization (UTC).
   - SHA-256 cryptographic hashing per log entry for evidentiary non-repudiation.

2. **Polymorphic Threat Detection Engine**
   - 7 built-in detection rules targeting adversary tactics across MITRE ATT&CK:
     - `BRUTE_FORCE_001`: Brute-force authentication and rapid account lockout.
     - `PRIV_ESC_001`: Privilege escalation and contextual sudo abuse.
     - `AUTH_FAILURE_001`: Repeated authentication failures with noise suppression.
     - `ACCOUNT_CREATE_001`: Contextual rapid account creation following anomalous activity.
     - `MALWARE_PERSIST_001`: Scheduled task and persistence mechanism manipulation.
     - `DATA_EXFIL_001`: Large outbound data transfer anomalies.
     - `SUSPICIOUS_CMD_001`: Execution of reconnaissance, credential dumping, or anti-forensic commands.
   - Sliding time-window correlation across events.
   - Deterministic duplicate prevention on repeated detection executions.

3. **Transparent Heuristic Threat Scoring & Severity Overrides**
   - Explicit mathematical detection scoring formulas ($0\text{--}100$).
   - Rule-defined severity escalation: For example, in `BRUTE_FORCE_001`:
     $$\text{Score} = 30 + (5 \times \text{failedAttempts}) + 20 = \mathbf{65}$$
     Overridden to **`CRITICAL`** with reason: *"Account lock following confirmed brute-force activity"*.
   - Rigorous academic honesty: No artificial score inflation to 75.

4. **Forensic Timeline Reconstruction**
   - Chronologically correlates disparate system, security, and application events.
   - Interleaves threat detections directly into the timeline.
   - Multi-parameter filtering by event type, IP, user, and severity.

5. **Chain of Custody & Evidence Inspection**
   - Direct foreign-key linkage between identified threats and exact triggering `LogEntry` records via `threat_evidence`.
   - Side-drawer inspection reveals raw log lines, extracted metadata, and cryptographic hashes.

6. **Automated PDF Incident Report Generation**
   - Generates official PDF forensic triage reports using **Apache PDFBox 3.x**.
   - Includes executive summaries, threat matrices, timeline highlights, and investigator sign-offs.

7. **SOC Command Center UI**
   - Built with React 19, TypeScript, and Tailwind CSS v4 in a dark SOC theme.
   - Real-time telemetry cards, interactive SVG charts (Recharts), and modular multi-tab investigation workspaces.

---

## 3. Java & Object-Oriented Programming (OOP) Excellence

SentinelForensic is designed to demonstrate computer science principles in Java 21 LTS:

- **Abstraction**: `ThreatDetectionRule` and `LogParser` interfaces define clean service contracts decoupled from concrete detection heuristics.
- **Inheritance & DRY**: `AbstractThreatDetectionRule` encapsulates sliding time-window math, deduplication algorithms, and evidence attachment. Concrete rules inherit and specialize detection logic.
- **Polymorphism**: The `DetectionEngineService` dynamically injects all Spring beans implementing `ThreatDetectionRule` (`List<ThreatDetectionRule>`) and dispatches detection polymorphically.
- **Encapsulation**: Domain models strictly enforce data integrity through private state, controlled accessors, and business invariants.
- **Robust Exception Handling**: Custom exception hierarchy (`ResourceNotFoundException`, `LogParsingException`, `ReportGenerationException`) handled by `@RestControllerAdvice` returning RFC 7807 problem details.
- **Modern Collections & Streams**: Intensive use of Java Streams (`filter`, `map`, `groupingBy`, `sorted`) for log slicing, aggregation, and timeline ordering.
- **Database Migrations**: Version-controlled Flyway migrations (`V1` to `V6`) ensure reproducible database schemas.

---

## 4. Technology Stack

| Layer | Technology | Version | Description |
| :--- | :--- | :--- | :--- |
| **Language** | Java | 21 LTS | Modern Java with records, pattern matching, and streams |
| **Backend Framework** | Spring Boot | 3.2.4 | Enterprise REST API and dependency injection |
| **Database** | PostgreSQL | 18 | Relational database with Flyway V1–V6 migrations |
| **ORM** | Spring Data JPA / Hibernate | 6.4 | Entity persistence and relational mappings |
| **PDF Engine** | Apache PDFBox | 3.0.1 | Programmatic PDF forensic report rendering |
| **Frontend Framework** | React | 19.0 | Component-based UI library |
| **Language** | TypeScript | 5.x | Type-safe frontend client |
| **Styling** | Tailwind CSS | v4 | Dark SOC theme styling |
| **Icons & Charts** | Lucide React / Recharts | Latest | High-fidelity SOC visuals |
| **Build Tools** | Maven & Vite | Latest | Java build lifecycle & instant frontend HMR |

---

## 5. System Setup & Run Instructions

### Prerequisites
- **Java 21 LTS** JDK installed and configured (`JAVA_HOME`).
- **Node.js 18+** & npm installed.
- **PostgreSQL 18** installed.

### Step 1: Database Initialization
1. Ensure PostgreSQL is active on port `5432`:
   ```bash
   postgres.exe -D "X:\pgdata"
   ```
2. Create database `sentinelforensic` (Flyway automatically applies migrations V1–V6 on startup):
   ```sql
   CREATE DATABASE sentinelforensic;
   ```

### Step 2: Backend Execution
Navigate to the backend directory and launch the Spring Boot server:
```powershell
cd backend
mvn spring-boot:run
```
*The backend boots on port `8081` (`http://localhost:8081`).*

### Step 3: Frontend Execution
Navigate to the frontend directory, install dependencies, and launch Vite:
```powershell
cd frontend
npm install
npm run dev
```
*The frontend boots on port `5173` (`http://localhost:5173`).*

---

## 6. Project Documentation Index

- **[System Architecture & Design (docs/ARCHITECTURE.md)](docs/ARCHITECTURE.md)**: Layered architecture, OOP class diagrams, sequence flows.
- **[Threat Rules Specification (docs/THREAT-RULES.md)](docs/THREAT-RULES.md)**: Detailed detection logic, formulas, sliding windows, and severity escalation rules.
- **[Database Schema Documentation (docs/DATABASE.md)](docs/DATABASE.md)**: Table definitions, foreign key constraints, indexes, and ER relationships.
- **[REST API Documentation (docs/API.md)](docs/API.md)**: Complete endpoint catalogue, query parameters, and JSON payloads.
- **[Evaluator Demonstration Guide (docs/DEMO.md)](docs/DEMO.md)**: Step-by-step scoring verification and presentation guide for evaluators.

---

## 7. Sample Data

Sample log files are provided in `sample-data/logs/` for instant evaluation:
- `sample_system.log`: Contains multi-stage SSH brute-force attack, account lockout, and rapid user creation.
- `sample_security.log`: Contains privilege escalation, sudo abuse, and defense evasion indicators.
- `sample_mixed.log`: Correlated mixed multi-vector attack stream.

---

## 8. License

Developed for educational demonstration, cybersecurity coursework, and digital forensics triage research.
