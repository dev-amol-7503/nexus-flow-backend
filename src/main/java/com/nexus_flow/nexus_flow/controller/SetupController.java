package com.nexus_flow.nexus_flow.controller;

import com.nexus_flow.nexus_flow.dto.SetupAdminRequest;
import com.nexus_flow.nexus_flow.entity.ERole;
import com.nexus_flow.nexus_flow.entity.Role;
import com.nexus_flow.nexus_flow.entity.User;
import com.nexus_flow.nexus_flow.repository.RoleRepository;
import com.nexus_flow.nexus_flow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/setup")
@CrossOrigin(origins = "*")
public class SetupController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.setup.invitation-code:MYADMIN123}")
    private String validInvitationCode;

    // ✅ STEP 1: Check if system needs setup - FIXED VERSION
    @GetMapping("/status")
    public ResponseEntity<?> getSetupStatus() {
        try {
            // First, ensure default roles exist
            ensureDefaultRolesExist();

            // Check if any admin user exists
            Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN).orElse(null);
            if (adminRole == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("canSetup", true);
                response.put("status", "NO_ROLES");
                response.put("message", "System needs initial setup - roles not found");
                return ResponseEntity.ok(response);
            }

            List<User> adminUsers = userRepository.findByRolesContaining(adminRole);
            boolean needsSetup = adminUsers.isEmpty();

            if (needsSetup) {
                Map<String, Object> response = new HashMap<>();
                response.put("canSetup", true);
                response.put("status", "NEEDS_SETUP");
                response.put("message", "No admin found. System needs setup.");
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("canSetup", false);
                response.put("status", "READY");
                response.put("message", "System is ready. Admin exists.");
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("canSetup", true);
            response.put("status", "ERROR");
            response.put("message", "Error checking setup status: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    // ✅ STEP 2: Create First Admin - FIXED VERSION WITH JSON RESPONSE
    @PostMapping("/create-first-admin")
    public ResponseEntity<?> createFirstAdmin(@RequestBody SetupAdminRequest request) {
        try {
            System.out.println("=== FIRST TIME ADMIN SETUP REQUEST ===");
            System.out.println("Username: " + request.getUsername());
            System.out.println("Email: " + request.getEmail());
            System.out.println("Invitation Code: " + request.getInvitationCode());

            // 🔒 SECURITY CHECK 1: Verify invitation code
            if (!validInvitationCode.equals(request.getInvitationCode())) {
                System.out.println("❌ Invalid invitation code");
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Error: Invalid invitation code");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // 🔒 SECURITY CHECK 2: Ensure default roles exist
            ensureDefaultRolesExist();

            // 🔒 SECURITY CHECK 3: Check if admin already exists
            Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN).orElse(null);
            if (adminRole != null) {
                List<User> existingAdmins = userRepository.findByRolesContaining(adminRole);
                if (!existingAdmins.isEmpty()) {
                    System.out.println("❌ Admin already exists");
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("success", false);
                    errorResponse.put("message", "Error: Admin user already exists. Setup completed.");
                    return ResponseEntity.badRequest().body(errorResponse);
                }
            }

            // 🔒 SECURITY CHECK 4: Check if username/email already taken
            if (userRepository.existsByUsername(request.getUsername())) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Error: Username already taken");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            if (userRepository.existsByEmail(request.getEmail())) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Error: Email already registered");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // ✅ CREATE ADMIN USER
            User adminUser = new User();
            adminUser.setFirstName(request.getFirstName());
            adminUser.setLastName(request.getLastName());
            adminUser.setUsername(request.getUsername());
            adminUser.setEmail(request.getEmail());
            adminUser.setPassword(passwordEncoder.encode(request.getPassword()));
            adminUser.setActive(true);

            // Assign ADMIN role - WITH PROPER ERROR HANDLING
            Role adminRoleToAssign = roleRepository.findByName(ERole.ROLE_ADMIN)
                    .orElseGet(() -> {
                        // If role doesn't exist, create it
                        System.out.println("⚠️ ROLE_ADMIN not found, creating it...");
                        Role newRole = new Role();
                        newRole.setName(ERole.ROLE_ADMIN);
                        newRole.setDescription("System Administrator");
                        return roleRepository.save(newRole);
                    });

            HashSet<Role> roles = new HashSet<>();
            roles.add(adminRoleToAssign);
            adminUser.setRoles(roles);

            // Save to database
            User savedAdmin = userRepository.save(adminUser);

            System.out.println("✅ FIRST ADMIN CREATED SUCCESSFULLY!");
            System.out.println("👤 Username: " + savedAdmin.getUsername());
            System.out.println("📧 Email: " + savedAdmin.getEmail());
            System.out.println("🎯 Role: " + adminRoleToAssign.getName());
            System.out.println("=========================================");

            // ✅ RETURN PROPER JSON RESPONSE
            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("success", true);
            successResponse.put("message", "First admin user created successfully! You can now login.");
            successResponse.put("username", savedAdmin.getUsername());
            successResponse.put("email", savedAdmin.getEmail());

            return ResponseEntity.ok(successResponse);

        } catch (Exception e) {
            System.out.println("❌ Setup error: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error during setup: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // ✅ NEW METHOD: Ensure default roles exist
    private void ensureDefaultRolesExist() {
        System.out.println("🔍 Checking if default roles exist...");

        // Check and create ROLE_ADMIN
        if (roleRepository.findByName(ERole.ROLE_ADMIN).isEmpty()) {
            System.out.println("➕ Creating ROLE_ADMIN...");
            Role adminRole = new Role();
            adminRole.setName(ERole.ROLE_ADMIN);
            adminRole.setDescription("System Administrator");
            roleRepository.save(adminRole);
        }

        // Check and create ROLE_PROJECT_MANAGER
        if (roleRepository.findByName(ERole.ROLE_PROJECT_MANAGER).isEmpty()) {
            System.out.println("➕ Creating ROLE_PROJECT_MANAGER...");
            Role pmRole = new Role();
            pmRole.setName(ERole.ROLE_PROJECT_MANAGER);
            pmRole.setDescription("Project Manager");
            roleRepository.save(pmRole);
        }

        // Check and create ROLE_TEAM_MEMBER
        if (roleRepository.findByName(ERole.ROLE_TEAM_MEMBER).isEmpty()) {
            System.out.println("➕ Creating ROLE_TEAM_MEMBER...");
            Role memberRole = new Role();
            memberRole.setName(ERole.ROLE_TEAM_MEMBER);
            memberRole.setDescription("Team Member");
            roleRepository.save(memberRole);
        }

        System.out.println("✅ All default roles verified/created");
    }

    // 📦 DTO Classes
    public static class SetupAdminRequest {
        private String invitationCode;
        private String firstName;
        private String lastName;
        private String username;
        private String email;
        private String password;

        // Getters and Setters
        public String getInvitationCode() { return invitationCode; }
        public void setInvitationCode(String invitationCode) { this.invitationCode = invitationCode; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}