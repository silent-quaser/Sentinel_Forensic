package com.sentinelforensic.controller;

import com.sentinelforensic.dto.LogEntryDto;
import com.sentinelforensic.dto.LogUploadResponse;
import com.sentinelforensic.dto.TimelineEventDto;
import com.sentinelforensic.service.LogService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
public class LogController {

    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    @PostMapping(value = "/investigations/{id}/logs", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<LogUploadResponse> uploadLog(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        LogUploadResponse response = logService.uploadLogFile(id, file);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/investigations/{id}/logs")
    public ResponseEntity<Page<LogEntryDto>> getInvestigationLogs(
            @PathVariable Long id,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<LogEntryDto> logs = logService.getInvestigationLogs(
                id, eventType, username, source, startTime, endTime, keyword, pageable);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/investigations/{id}/timeline")
    public ResponseEntity<List<TimelineEventDto>> getTimeline(@PathVariable Long id) {
        List<TimelineEventDto> timeline = logService.getTimeline(id);
        return ResponseEntity.ok(timeline);
    }

    @GetMapping("/logs")
    public ResponseEntity<Page<LogEntryDto>> searchGlobalLogs(
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<LogEntryDto> logs = logService.searchGlobalLogs(eventType, username, source, keyword, pageable);
        return ResponseEntity.ok(logs);
    }
}
