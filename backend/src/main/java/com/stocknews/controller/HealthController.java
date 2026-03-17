package com.stocknews.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Health check controller for monitoring and deploy verification.
 * All endpoints are public (configured in SecurityConfig).
 */
@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "Health", description = "Application health and version checks")
public class HealthController {

    private static final Instant STARTUP_TIME = Instant.now();
    private static final String BUILD_VERSION = "v7-final";

    /**
     * Returns application health status with startup time and build version.
     * Useful for verifying which version of the app is deployed.
     *
     * @return health status with version and uptime info
     */
    @GetMapping
    @Operation(summary = "Health check", description = "Returns application health status and build version")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "version", BUILD_VERSION,
                "startedAt", STARTUP_TIME.toString(),
                "uptime", java.time.Duration.between(STARTUP_TIME, Instant.now()).toString()
        ));
    }
}
