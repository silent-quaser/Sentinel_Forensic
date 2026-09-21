package com.sentinelforensic.parser;

import com.sentinelforensic.model.LogEntry;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SystemLogParser implements LogParser {

    // Matches: 2026-07-21 09:15:32 EVENT_TYPE Username [optional extra text]
    // Also matches: 2026-07-21 09:15:32 [Source] EVENT_TYPE Username
    private static final Pattern PATTERN = Pattern.compile(
        "^(\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2})\\s+(?:\\[([^\\]]+)\\]\\s+)?([A-Za-z0-9_-]+)\\s+([^\\s]+)(?:\\s+(.*))?$"
    );

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public boolean supports(String identifier) {
        if (identifier == null) return false;
        String lower = identifier.toLowerCase();
        if (lower.endsWith(".log") || lower.endsWith(".txt") || lower.endsWith(".csv")) {
            return true;
        }
        return PATTERN.matcher(identifier.trim()).matches();
    }

    @Override
    public List<LogEntry> parse(BufferedReader reader, Long investigationId) throws IOException {
        return parseWithReport(reader, investigationId, "unnamed.log").getEntries();
    }

    @Override
    public ParseResult parseWithReport(BufferedReader reader, Long investigationId, String fileName) throws IOException {
        List<LogEntry> entries = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        int totalLines = 0;
        int parsedCount = 0;
        int skippedCount = 0;

        String line;
        while ((line = reader.readLine()) != null) {
            totalLines++;
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue; // Skip blank lines without penalty
            }

            Matcher matcher = PATTERN.matcher(trimmed);
            if (matcher.matches()) {
                String timestampStr = matcher.group(1);
                String sourceGroup = matcher.group(2);
                String eventType = matcher.group(3);
                String username = matcher.group(4);
                String extraDetails = matcher.group(5);

                try {
                    LocalDateTime timestamp = LocalDateTime.parse(timestampStr, FORMATTER);
                    LogEntry entry = new LogEntry();
                    entry.setInvestigationId(investigationId);
                    entry.setTimestamp(timestamp);
                    entry.setEventType(eventType.toUpperCase());
                    entry.setUsername(username);
                    entry.setSource(sourceGroup != null ? sourceGroup : "System");
                    entry.setSourceType("SecurityLog");
                    entry.setRawMessage(line);

                    // Generate contextual description and severity
                    enrichEntryMetadata(entry, extraDetails);

                    entries.add(entry);
                    parsedCount++;
                } catch (DateTimeParseException e) {
                    skippedCount++;
                    warnings.add("Line " + totalLines + ": Invalid timestamp format '" + timestampStr + "'. Expected YYYY-MM-DD HH:MM:SS.");
                }
            } else {
                skippedCount++;
                warnings.add("Line " + totalLines + ": Malformed line syntax. Could not parse security event from: \"" + truncate(trimmed, 60) + "\"");
            }
        }

        return new ParseResult(entries, totalLines, parsedCount, skippedCount, warnings, errors);
    }

    private void enrichEntryMetadata(LogEntry entry, String extraDetails) {
        String type = entry.getEventType();
        String user = entry.getUsername();

        switch (type) {
            case "LOGIN_SUCCESS":
                entry.setSeverity("INFO");
                entry.setDescription("User '" + user + "' logged in successfully.");
                break;
            case "LOGIN_FAILED":
                entry.setSeverity("WARN");
                entry.setDescription("Authentication failed for user '" + user + "'.");
                break;
            case "ACCOUNT_LOCKED":
                entry.setSeverity("ERROR");
                entry.setDescription("Account locked for user '" + user + "' due to multiple failed authentication attempts.");
                break;
            case "USER_CREATED":
                entry.setSeverity("INFO");
                entry.setDescription("New user account '" + user + "' was created.");
                break;
            case "PASSWORD_CHANGED":
                entry.setSeverity("INFO");
                entry.setDescription("Password changed for user '" + user + "'.");
                break;
            case "LOGOUT":
                entry.setSeverity("INFO");
                entry.setDescription("User '" + user + "' logged out.");
                break;
            case "ROLE_CHANGED":
            case "PRIVILEGE_GRANTED":
            case "ADMIN_ASSIGNED":
                entry.setSeverity("WARN");
                entry.setDescription("Privilege modification event for user '" + user + "': " + type + (extraDetails != null ? " (" + extraDetails + ")" : ""));
                break;
            default:
                entry.setSeverity("INFO");
                entry.setDescription("Event " + type + " recorded for user '" + user + "'." + (extraDetails != null ? " " + extraDetails : ""));
                break;
        }

        if (extraDetails != null && !extraDetails.isBlank()) {
            if (extraDetails.contains("ip=") || extraDetails.contains("IP:")) {
                entry.setIpAddress(extractValue(extraDetails, "ip="));
            }
        }
    }

    private String extractValue(String text, String prefix) {
        int idx = text.indexOf(prefix);
        if (idx != -1) {
            String sub = text.substring(idx + prefix.length()).trim();
            int end = sub.indexOf(" ");
            return end != -1 ? sub.substring(0, end) : sub;
        }
        return null;
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        return text.length() <= maxLen ? text : text.substring(0, maxLen) + "...";
    }
}