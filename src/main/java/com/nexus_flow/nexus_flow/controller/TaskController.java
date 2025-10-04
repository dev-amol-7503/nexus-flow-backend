package com.nexus_flow.nexus_flow.controller;

import com.nexus_flow.nexus_flow.dto.ApiResponse;
import com.nexus_flow.nexus_flow.dto.CreateTaskRequest;
import com.nexus_flow.nexus_flow.entity.Task;
import com.nexus_flow.nexus_flow.entity.User;
import com.nexus_flow.nexus_flow.service.TaskService;
import com.nexus_flow.nexus_flow.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:3000"})
public class TaskController {

    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserService userService;

    @GetMapping("/my-tasks")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<Task>>> getMyTasks(Authentication authentication) {
        try {
            String username = authentication.getName();
            logger.info("Fetching tasks for user: {}", username);

            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<Task> tasks = taskService.getTasksByUser(user);
            ApiResponse<List<Task>> response = ApiResponse.success("Tasks fetched successfully", tasks);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching user tasks", e);
            throw e;
        }
    }

    @GetMapping("/project/{projectId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<Task>>> getTasksByProject(@PathVariable Long projectId) {
        try {
            logger.info("Fetching tasks for project: {}", projectId);
            List<Task> tasks = taskService.getTasksByProject(projectId);
            ApiResponse<List<Task>> response = ApiResponse.success("Project tasks fetched successfully", tasks);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching project tasks for project: {}", projectId, e);
            throw e;
        }
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Task>> createTask(
            @Valid @RequestBody CreateTaskRequest createRequest,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            logger.info("Creating task for user: {}", username);

            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Task task = taskService.createTask(createRequest, user);
            ApiResponse<Task> response = ApiResponse.created("Task created successfully", task);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error creating task", e);
            throw e;
        }
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Task>> updateTaskStatus(
            @PathVariable Long id,
            @RequestBody UpdateTaskStatusRequest statusRequest,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            logger.info("Updating task status for task: {} by user: {}", id, username);

            Task task = taskService.updateTaskStatus(id, statusRequest.getStatus());
            ApiResponse<Task> response = ApiResponse.success("Task status updated successfully", task);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error updating task status for task: {}", id, e);
            throw e;
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Task>> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody CreateTaskRequest updateRequest,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            logger.info("Updating task with id: {} for user: {}", id, username);

            Task task = taskService.updateTask(id, updateRequest);
            ApiResponse<Task> response = ApiResponse.success("Task updated successfully", task);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error updating task with id: {}", id, e);
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteTask(@PathVariable Long id) {
        try {
            logger.info("Deleting task with id: {}", id);
            taskService.deleteTask(id);
            ApiResponse<Void> response = ApiResponse.success("Task deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error deleting task with id: {}", id, e);
            throw e;
        }
    }

    // Inner class for status update request
    public static class UpdateTaskStatusRequest {
        private Task.TaskStatus status;

        public Task.TaskStatus getStatus() {
            return status;
        }

        public void setStatus(Task.TaskStatus status) {
            this.status = status;
        }
    }
}