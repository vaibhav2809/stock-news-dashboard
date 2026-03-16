package com.stocknews.controller;

import com.stocknews.dto.AlertResponse;
import com.stocknews.dto.CreateAlertRequest;
import com.stocknews.security.AuthenticatedUser;
import com.stocknews.service.AlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing user-configured stock news alerts.
 * Supports creating, listing, toggling, and deleting alerts.
 * All endpoints require JWT authentication.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
@Tag(name = "Alerts", description = "Stock news alert management")
public class AlertController {

    private final AlertService alertService;

    /**
     * Lists all alerts configured by the authenticated user.
     *
     * @return list of alert response DTOs
     */
    @GetMapping
    @Operation(summary = "Get alerts", description = "List all stock news alerts for the authenticated user")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved alerts")
    public ResponseEntity<List<AlertResponse>> getAlerts() {
        log.info("GET /api/v1/alerts");
        final List<AlertResponse> alerts = alertService.getAlerts(AuthenticatedUser.getUserId());
        return ResponseEntity.ok(alerts);
    }

    /**
     * Creates a new stock news alert for the authenticated user.
     *
     * @param request the alert configuration
     * @return the created alert with 201 status
     */
    @PostMapping
    @Operation(summary = "Create alert", description = "Create a new stock news alert")
    @ApiResponse(responseCode = "201", description = "Alert created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid alert configuration")
    public ResponseEntity<AlertResponse> createAlert(@Valid @RequestBody CreateAlertRequest request) {
        log.info("POST /api/v1/alerts — symbol={}, alertType={}", request.getSymbol(), request.getAlertType());
        final AlertResponse response = alertService.createAlert(AuthenticatedUser.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Toggles the active/inactive state of an alert.
     *
     * @param id the alert ID to toggle
     * @return the updated alert
     */
    @PatchMapping("/{id}/toggle")
    @Operation(summary = "Toggle alert", description = "Toggle the active state of an alert")
    @ApiResponse(responseCode = "200", description = "Alert toggled successfully")
    @ApiResponse(responseCode = "404", description = "Alert not found")
    public ResponseEntity<AlertResponse> toggleAlert(
            @Parameter(description = "Alert ID", example = "1")
            @PathVariable Long id
    ) {
        log.info("PATCH /api/v1/alerts/{}/toggle", id);
        final AlertResponse response = alertService.toggleAlert(AuthenticatedUser.getUserId(), id);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes an alert belonging to the authenticated user.
     *
     * @param id the alert ID to delete
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete alert", description = "Delete a stock news alert")
    @ApiResponse(responseCode = "204", description = "Alert deleted successfully")
    @ApiResponse(responseCode = "404", description = "Alert not found")
    public ResponseEntity<Void> deleteAlert(
            @Parameter(description = "Alert ID", example = "1")
            @PathVariable Long id
    ) {
        log.info("DELETE /api/v1/alerts/{}", id);
        alertService.deleteAlert(AuthenticatedUser.getUserId(), id);
        return ResponseEntity.noContent().build();
    }
}
