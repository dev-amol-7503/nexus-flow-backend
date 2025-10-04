package com.nexus_flow.nexus_flow.service;

import com.nexus_flow.nexus_flow.entity.User;
import com.nexus_flow.nexus_flow.entity.Role;
import com.nexus_flow.nexus_flow.entity.ERole;
import com.nexus_flow.nexus_flow.repository.UserRepository;
import com.nexus_flow.nexus_flow.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Check if username exists
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    // Check if email exists
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    // Create new user
    public User createUser(com.nexus_flow.nexus_flow.dto.RegisterRequest registerRequest) {
        User user = new User();
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setActive(true);

        // Default role set karo - ROLE_TEAM_MEMBER (Flyway migration ke saath match karo)
        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName(ERole.ROLE_TEAM_MEMBER) // Change TEAM_MEMBER to ROLE_TEAM_MEMBER
                .orElseThrow(() -> new RuntimeException("Error: Role not found."));
        roles.add(userRole);
        user.setRoles(roles);

        return userRepository.save(user);
    }

    // Find user by username
    public java.util.Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}