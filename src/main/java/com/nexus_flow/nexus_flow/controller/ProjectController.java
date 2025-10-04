package com.nexus_flow.nexus_flow.controller;

import com.nexus_flow.nexus_flow.dto.ApiResponse;
import com.nexus_flow.nexus_flow.dto.CreateProjectRequest;
import com.nexus_flow.nexus_flow.entity.Project;
import com.nexus_flow.nexus_flow.entity.User;
import com.nexus_flow.nexus_flow.service.ProjectService;
import com.nexus_flow.nexus_flow.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:3000"})
public class ProjectController {

    private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserService userService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<Project>>> getAllProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            logger.info("Fetching projects for user: {}, page: {}, size: {}", username, page, size);

            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String[] sortParams = sort.split(",");
            Sort.Direction direction = sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1])
                    ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));

            Page<Project> projects = projectService.getProjectsByUser(user, pageable);
            ApiResponse<Page<Project>> response = ApiResponse.success("Projects fetched successfully", projects);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching projects", e);
            throw e;
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Project>> getProjectById(@PathVariable Long id) {
        try {
            logger.info("Fetching project with id: {}", id);
            Project project = projectService.getProjectById(id)
                    .orElseThrow(() -> new RuntimeException("Project not found"));
            ApiResponse<Project> response = ApiResponse.success("Project fetched successfully", project);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching project with id: {}", id, e);
            throw e;
        }
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Project>> createProject(
            @Valid @RequestBody CreateProjectRequest createRequest,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            logger.info("Creating project for user: {}", username);

            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Project project = projectService.createProject(createRequest, user);
            ApiResponse<Project> response = ApiResponse.created("Project created successfully", project);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error creating project", e);
            throw e;
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Project>> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody CreateProjectRequest updateRequest,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            logger.info("Updating project with id: {} for user: {}", id, username);

            Project project = projectService.updateProject(id, updateRequest);
            ApiResponse<Project> response = ApiResponse.success("Project updated successfully", project);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error updating project with id: {}", id, e);
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteProject(@PathVariable Long id) {
        try {
            logger.info("Deleting project with id: {}", id);
            projectService.deleteProject(id);
            ApiResponse<Void> response = ApiResponse.success("Project deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error deleting project with id: {}", id, e);
            throw e;
        }
    }

    @GetMapping("/statistics")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Object>> getProjectStatistics() {
        try {
            logger.info("Fetching project statistics");
            Object stats = projectService.getProjectStatistics();
            ApiResponse<Object> response = ApiResponse.success("Project statistics fetched successfully", stats);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching project statistics", e);
            throw e;
        }
    }
}