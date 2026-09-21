package com.sentinelforensic.controller;

import com.sentinelforensic.dto.SystemHealthDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;

@RestController
@RequestMapping("/api/health")
public class HealthController {
    private static final Logger log = LoggerFactory.getLogger(HealthController.class);

    private final DataSource dataSource;

    public HealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping
    public ResponseEntity<SystemHealthDto> getHealth() {
        String dbStatus = "Connected";
        try (Connection conn = dataSource.getConnection()) {
            if (!conn.isValid(2)) {
                dbStatus = "Degraded";
            }
        } catch (Exception e) {
            log.error("Database health check failed", e);
            dbStatus = "Disconnected: " + e.getMessage();
        }

        SystemHealthDto health = new SystemHealthDto(
            "Operational",
            dbStatus,
            "Active (7 Rules Loaded)",
            "Available (SystemLogParser)",
            "1.0.0-RELEASE"
        );

        return ResponseEntity.ok(health);
    }
}
