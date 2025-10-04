package com.nexus_flow.nexus_flow.controller;

import com.nexus_flow.nexus_flow.dto.ApiResponse;
import com.nexus_flow.nexus_flow.dto.UpdateUserRequest;
import com.nexus_flow.nexus_flow.entity.User;
import com.nexus_flow.nexus_flow.exception.ResourceNotFoundException;
import com.nexus_flow.nexus_flow.service.DashboardService;
import com.nexus_flow.nexus_flow.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:3000"})
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private UserService userService;

    // Dashboard and Statistics
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAdminStats() {
        try {
            logger.info("Fetching admin statistics");

            Map<String, Object> stats = new HashMap<>();

            // User statistics
            stats.put("totalUsers", userService.countUsers());
            stats.put("activeUsers", userService.countActiveUsers());

            // Project and task statistics
            Map<String, Object> dashboardStats = dashboardService.getDashboardStats();
            stats.putAll(dashboardStats);

            // System statistics
            stats.put("storageUsed", "2.5 GB");
            stats.put("systemUptime", "99.9%");

            ApiResponse<Map<String, Object>> response = ApiResponse.success("Admin stats fetched successfully", stats);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching admin stats", e);
            throw e;
        }
    }

    // User Management - List all users
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        try {
            logger.info("Fetching all users for admin");
            List<User> users = userService.getAllUsers();
            ApiResponse<List<User>> response = ApiResponse.success("Users fetched successfully", users);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching users for admin", e);
            throw e;
        }
    }

    // User Management - Paginated users
    @GetMapping("/users/page")
    public ResponseEntity<ApiResponse<Page<User>>> getAllUsersPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            logger.info("Fetching paginated users for admin, page: {}, size: {}", page, size);
            Pageable pageable = PageRequest.of(page, size);
            Page<User> users = userService.getAllUsers(pageable);
            ApiResponse<Page<User>> response = ApiResponse.success("Users fetched successfully", users);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching paginated users for admin", e);
            throw e;
        }
    }

    // User Management - Get user by ID
    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable Long id) {
        try {
            logger.info("Fetching user by id for admin: {}", id);
            User user = userService.getUserById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
            ApiResponse<User> response = ApiResponse.success("User fetched successfully", user);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            logger.warn("User not found with id: {}", id);
            throw e;
        } catch (Exception e) {
            logger.error("Error fetching user by id for admin: {}", id, e);
            throw e;
        }
    }

    // User Management - Update user
    @PutMapping("/users/{id}")
    public ResponseEntity<ApiResponse<User>> updateUser(@PathVariable Long id,
                                                        @RequestBody UpdateUserRequest updateRequest) {
        try {
            logger.info("Updating user for admin, id: {}", id);
            User updatedUser = userService.updateUser(id, updateRequest);
            ApiResponse<User> response = ApiResponse.success("User updated successfully", updatedUser);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error updating user for admin, id: {}", id, e);
            throw e;
        }
    }

    // User Management - Delete user
    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        try {
            logger.info("Deleting user for admin, id: {}", id);
            userService.deleteUser(id);
            ApiResponse<Void> response = ApiResponse.success("User deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error deleting user for admin, id: {}", id, e);
            throw e;
        }
    }

    // User Management - Toggle user status
    @PatchMapping("/users/{id}/toggle-status")
    public ResponseEntity<ApiResponse<User>> toggleUserStatus(@PathVariable Long id) {
        try {
            logger.info("Toggling user status for admin, id: {}", id);
            User updatedUser = userService.toggleUserStatus(id);
            ApiResponse<User> response = ApiResponse.success("User status toggled successfully", updatedUser);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error toggling user status for admin, id: {}", id, e);
            throw e;
        }
    }

    // User Management - Update user roles
    @PatchMapping("/users/{id}/roles")
    public ResponseEntity<ApiResponse<User>> updateUserRoles(@PathVariable Long id,
                                                             @RequestBody Set<Long> roleIds) {
        try {
            logger.info("Updating user roles for admin, id: {}", id);
            User updatedUser = userService.updateUserRoles(id, roleIds);
            ApiResponse<User> response = ApiResponse.success("User roles updated successfully", updatedUser);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error updating user roles for admin, id: {}", id, e);
            throw e;
        }
    }

    // User Management - Search users
    @GetMapping("/users/search")
    public ResponseEntity<ApiResponse<List<User>>> searchUsers(@RequestParam String q) {
        try {
            logger.info("Searching users for admin, query: {}", q);
            List<User> users = userService.searchUsers(q);
            ApiResponse<List<User>> response = ApiResponse.success("Users searched successfully", users);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error searching users for admin, query: {}", q, e);
            throw e;
        }
    }

    // Additional user management endpoints can be added here as needed
    // For example:
    // - Get user activities
    // - Get user projects
    // - Reset user password
    // - Export users
}