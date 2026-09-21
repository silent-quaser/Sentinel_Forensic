package com.sentinelforensic.parser;

import com.sentinelforensic.model.LogEntry;
import java.util.ArrayList;
import java.util.List;

public class ParseResult {
    private final List<LogEntry> entries;
    private final int totalLines;
    private final int parsedCount;
    private final int skippedCount;
    private final List<String> warnings;
    private final List<String> errors;

    public ParseResult(List<LogEntry> entries, int totalLines, int parsedCount, int skippedCount, List<String> warnings, List<String> errors) {
        this.entries = entries != null ? entries : new ArrayList<>();
        this.totalLines = totalLines;
        this.parsedCount = parsedCount;
        this.skippedCount = skippedCount;
        this.warnings = warnings != null ? warnings : new ArrayList<>();
        this.errors = errors != null ? errors : new ArrayList<>();
    }

    public List<LogEntry> getEntries() { return entries; }
    public int getTotalLines() { return totalLines; }
    public int getParsedCount() { return parsedCount; }
    public int getSkippedCount() { return skippedCount; }
    public List<String> getWarnings() { return warnings; }
    public List<String> getErrors() { return errors; }
}
