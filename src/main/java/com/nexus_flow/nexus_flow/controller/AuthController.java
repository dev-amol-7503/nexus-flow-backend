package com.nexus_flow.nexus_flow.controller;

import com.nexus_flow.nexus_flow.dto.*;
import com.nexus_flow.nexus_flow.entity.User;
import com.nexus_flow.nexus_flow.exception.AuthenticationException;
import com.nexus_flow.nexus_flow.exception.BusinessException;
import com.nexus_flow.nexus_flow.exception.ResourceNotFoundException;
import com.nexus_flow.nexus_flow.exception.ValidationException;
import com.nexus_flow.nexus_flow.security.JwtTokenUtil;
import com.nexus_flow.nexus_flow.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:3000"})
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            logger.info("Registration request for user: {}", registerRequest.getUsername());

            // Check if username already exists
            if (userService.existsByUsername(registerRequest.getUsername())) {
                throw new ValidationException("Username is already taken");
            }

            // Check if email already exists
            if (userService.existsByEmail(registerRequest.getEmail())) {
                throw new ValidationException("Email is already in use");
            }

            // Create new user
            User user = userService.createUser(registerRequest);

            logger.info("User registered successfully: {}", registerRequest.getUsername());

            // ✅ Structured response with user data
            AuthResponse authResponse = user.toAuthResponse();
            ApiResponse<AuthResponse> response = ApiResponse.created(
                    "User registered successfully!",
                    authResponse
            );

            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (ValidationException e) {
            // ✅ Alag catch block for ValidationException
            logger.warn("Validation error during registration: {}", e.getMessage());
            throw e;
        } catch (BusinessException e) {
            // ✅ Alag catch block for BusinessException
            logger.warn("Business error during registration: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during registration: {}", registerRequest.getUsername(), e);
            throw new BusinessException("Registration failed due to unexpected error");
        }
    }
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponse>> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            logger.info("Login attempt for user: {}", loginRequest.getUsername());

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String jwt = jwtTokenUtil.generateToken(userDetails);

            // Get user details from service
            User user = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("User", userDetails.getUsername()));

            // Check if user is active
            if (!user.isActive()) {
                throw new AuthenticationException("Account is deactivated");
            }

            // Convert Set to List for JwtResponse
            List<com.nexus_flow.nexus_flow.entity.Role> rolesList = new ArrayList<>(user.getRoles());

            JwtResponse jwtResponse = new JwtResponse(
                    jwt,
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    rolesList
            );

            logger.info("User logged in successfully: {}", loginRequest.getUsername());

            ApiResponse<JwtResponse> response = ApiResponse.success(
                    "Login successful",
                    jwtResponse
            );

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            logger.warn("Invalid login attempt for user: {}", loginRequest.getUsername());
            throw new AuthenticationException("Invalid username or password");
        } catch (ResourceNotFoundException e) {
            logger.warn("User not found during login: {}", loginRequest.getUsername());
            throw e;
        } catch (AuthenticationException e) {
            logger.warn("Authentication failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during login: {}", loginRequest.getUsername(), e);
            throw new BusinessException("Login failed due to unexpected error");
        }
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<User>> getCurrentUser(Authentication authentication) {
        try {
            String username = authentication.getName();
            logger.info("Fetching current user: {}", username);

            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("User", username));

            ApiResponse<User> response = ApiResponse.success("User fetched successfully", user);
            return ResponseEntity.ok(response);

        } catch (ResourceNotFoundException e) {
            logger.warn("User not found: {}", authentication.getName());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error fetching current user: {}", authentication.getName(), e);
            throw new BusinessException("Failed to fetch user data");
        }
    }
}