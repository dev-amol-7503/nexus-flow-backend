package com.nexus_flow.nexus_flow.controller;

import com.nexus_flow.nexus_flow.dto.ApiResponse;
import com.nexus_flow.nexus_flow.service.DashboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:3000"})
public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/stats")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardStats() {
        try {
            logger.info("Fetching dashboard statistics");
            Map<String, Object> stats = dashboardService.getDashboardStats();
            ApiResponse<Map<String, Object>> response = ApiResponse.success("Dashboard stats fetched successfully", stats);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching dashboard stats", e);
            throw e;
        }
    }
}