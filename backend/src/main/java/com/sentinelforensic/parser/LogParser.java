package com.sentinelforensic.parser;

import com.sentinelforensic.model.LogEntry;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

public interface LogParser {
    boolean supports(String fileNameOrSampleLine);
    ParseResult parseWithReport(BufferedReader reader, Long investigationId, String fileName) throws IOException;
    List<LogEntry> parse(BufferedReader reader, Long investigationId) throws IOException;
}