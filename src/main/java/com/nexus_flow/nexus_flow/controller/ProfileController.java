package com.nexus_flow.nexus_flow.controller;

import com.nexus_flow.nexus_flow.dto.ApiResponse;
import com.nexus_flow.nexus_flow.dto.UpdateProfileRequest;
import com.nexus_flow.nexus_flow.entity.User;
import com.nexus_flow.nexus_flow.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:3000"})
public class ProfileController {

    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);

    @Autowired
    private UserService userService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<User>> getCurrentUserProfile(Authentication authentication) {
        try {
            String username = authentication.getName();
            logger.info("Fetching profile for user: {}", username);

            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            ApiResponse<User> response = ApiResponse.success("Profile fetched successfully", user);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching user profile", e);
            throw e;
        }
    }

    @PutMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<User>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest updateRequest,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            logger.info("Updating profile for user: {}", username);

            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Use the new profile update method
            User updatedUser = userService.updateUserProfile(user.getId(), updateRequest);

            ApiResponse<User> response = ApiResponse.success("Profile updated successfully", updatedUser);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error updating user profile", e);
            throw e;
        }
    }
}