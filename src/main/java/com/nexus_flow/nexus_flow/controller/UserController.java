/*package com.nexus_flow.nexus_flow.controller;


import com.nexus_flow.nexus_flow.dto.UpdateUserRequest;
import com.nexus_flow.nexus_flow.entity.User;
import com.nexus_flow.nexus_flow.exception.ResourceNotFoundException;
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

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    // Keep only the basic endpoints, move others to AdminController
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("Error in getAllUsers", e);
            throw e;
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        try {
            User user = userService.getUserById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("User", id.toString()));
            return ResponseEntity.ok(user);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error in getUserById for id: {}", id, e);
            throw e;
        }
    }
}

*/
