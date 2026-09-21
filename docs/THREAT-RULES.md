# SentinelForensic Threat Detection Rules Catalog

SentinelForensic features a deterministic, rule-based cybersecurity correlation engine designed to analyze time-series security events. Every rule operates over a sliding temporal window, links exact causative `LogEntry` records via `ThreatEvidence`, and applies transparent scoring logic without synthetic AI percentages.

---

## Threat Scoring Model

Scores reflect heuristic certainty and correlation weight based on objective evidence:

| Score Range | Base Severity | Response Recommendation |
| :--- | :--- | :--- |
| **0 – 24** | **LOW** | Informational event logging; periodic audit. |
| **25 – 49** | **MEDIUM** | Standard investigator triage; monitor subject profile. |
| **50 – 74** | **HIGH** | Priority review; inspect associated identity streams. |
| **75 – 100+** | **CRITICAL** | Immediate response; isolate affected endpoint/credential. |

---

## 1. Brute Force Authentication Rule (`BRUTE_FORCE_001`)

- **Category**: Authentication & Access Control
- **Default Threshold**: 3 failed login attempts
- **Default Sliding Window**: 10 minutes (600 seconds)
- **Target Event Sequence**: `LOGIN_FAILED` $\ge 3$ for the same subject within the window, optionally followed by `ACCOUNT_LOCKED`.

### Scoring Formula
$$\text{Score} = 30 + (5 \times \text{failedAttempts}) + \text{accountLockBonus}$$
- Base Score: $+30$
- Per Failed Attempt: $+5$
- Account Lockout Bonus: $+20$

### Mandatory Severity Override
When a confirmed brute-force sequence of 3 failed attempts is followed by `ACCOUNT_LOCKED` within the window:
- Calculated Score: $30 + (5 \times 3) + 20 = 65$
- **Final Severity**: **CRITICAL** (Explicitly overridden from HIGH)
- **Escalation Reason**: `"Account lock following confirmed brute-force activity"`
- **Integrity Guarantee**: The application does **not** falsely inflate the score to 75. It reports `Score: 65`, `Severity: CRITICAL`, and the exact audit rationale.

---

## 2. Authentication Failure Threshold (`AUTH_FAILURE_001`)

- **Category**: Credential Auditing
- **Default Threshold**: 2 failed login attempts
- **Default Sliding Window**: 30 minutes (1800 seconds)
- **Target Event**: Multiple authentication failures for a user within an extended operational window.

### Scoring Formula
$$\text{Score} = 15 + (5 \times \text{failedAttempts})$$
- Default Score (2 failures): $25$ (`MEDIUM`)

### Deduplication & Subsumption
To prevent redundant alert clutter, if a set of failed login events is already captured and subsumed by a triggered `BRUTE_FORCE_001` detection on the same user, `AUTH_FAILURE_001` is suppressed.

---

## 3. Account Creation Following Authentication Failures (`ACCOUNT_CREATE_001`)

- **Category**: Persistence & Lateral Movement
- **Default Threshold**: 2 preceding failures within 15 minutes before user creation
- **Default Sliding Window**: 15 minutes (900 seconds)
- **Target Event Sequence**: `USER_CREATED` occurring shortly after authentication failures in the same environment.

### Scoring Formula
$$\text{Score} = 20 + 20 = 40\quad (\text{Severity: MEDIUM})$$

### Contextual Evaluation Rationale
Does not rely on ambiguous "off-hours" timestamps (which vary across time zones). Evaluates explicit temporal correlation between failed access attempts and rapid identity provisioning.

---

## 4. Rapid Password Modification (`PASSWORD_CHANGE_001`)

- **Category**: Credential Tampering
- **Default Threshold**: 1 password modification within 10 minutes of failed login
- **Default Sliding Window**: 10 minutes (600 seconds)
- **Target Event Sequence**: `PASSWORD_CHANGED` following a `LOGIN_FAILED` event for the same username.

### Scoring Formula
$$\text{Score} = 35\quad (\text{Severity: MEDIUM})$$

---

## 5. Successful Authentication Post-Failure (`AUTH_SUCCESS_AFTER_FAILURE_001`)

- **Category**: Account Compromise / Password Guessing
- **Default Threshold**: 2 failures followed by 1 success
- **Default Sliding Window**: 10 minutes (600 seconds)
- **Target Event Sequence**: `LOGIN_FAILED` $\ge 2$ immediately followed by `LOGIN_SUCCESS` for the same user.

### Scoring Formula
$$\text{Score} = 30 + (5 \times \text{failedAttempts}) = 40\quad (\text{Severity: MEDIUM})$$

---

## 6. High Frequency Event Burst (`EVENT_BURST_001`)

- **Category**: Automation / DoS / Scripted Activity
- **Default Threshold**: 10 events within 60 seconds
- **Default Sliding Window**: 1 minute (60 seconds)
- **Target Event**: Sudden velocity spike across events indicating automated tooling.

### Scoring Formula
$$\text{Score} = 55\quad (\text{Severity: HIGH})$$

---

## 7. Contextual Privilege Escalation (`PRIV_ESC_001`)

- **Category**: Privilege Abuse
- **Default Threshold**: 1 event
- **Default Sliding Window**: Immediate (single record or paired with preceding failed auth)
- **Target Event**: `PRIVILEGE_ESCALATED`

### Scoring Formula
$$\text{Score} = 60 + 15\ (\text{if preceded by auth failure}) = 75\quad (\text{Severity: HIGH to CRITICAL})$$

### Authorization State Accuracy
Does not assert whether the escalation was authorized or unauthorized unless authorization status is explicitly present in the log attributes or metadata. Evaluates the operational context of the transition.
