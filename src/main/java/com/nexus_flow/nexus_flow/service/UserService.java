package com.nexus_flow.nexus_flow.service;

import com.nexus_flow.nexus_flow.dto.RegisterRequest;
import com.nexus_flow.nexus_flow.dto.UpdateProfileRequest;
import com.nexus_flow.nexus_flow.dto.UpdateUserRequest;
import com.nexus_flow.nexus_flow.entity.User;
import com.nexus_flow.nexus_flow.entity.Role;
import com.nexus_flow.nexus_flow.entity.ERole;
import com.nexus_flow.nexus_flow.exception.BusinessException;
import com.nexus_flow.nexus_flow.exception.ResourceNotFoundException;
import com.nexus_flow.nexus_flow.exception.ValidationException;
import com.nexus_flow.nexus_flow.repository.UserRepository;
import com.nexus_flow.nexus_flow.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public boolean existsByUsername(String username) {
        try {
            return userRepository.existsByUsername(username);
        } catch (Exception e) {
            logger.error("Error checking username existence: {}", username, e);
            throw new BusinessException("Error checking username availability");
        }
    }

    public boolean existsByEmail(String email) {
        try {
            return userRepository.existsByEmail(email);
        } catch (Exception e) {
            logger.error("Error checking email existence: {}", email, e);
            throw new BusinessException("Error checking email availability");
        }
    }

    public User createUser(RegisterRequest registerRequest) {
        try {
            logger.info("Creating new user: {}", registerRequest.getUsername());

            // Validate input
            if (registerRequest.getPassword().length() < 6) {
                throw new ValidationException("Password must be at least 6 characters long");
            }

            User user = new User();
            user.setFirstName(registerRequest.getFirstName());
            user.setLastName(registerRequest.getLastName());
            user.setUsername(registerRequest.getUsername());
            user.setEmail(registerRequest.getEmail());
            user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
            user.setActive(true);

            // Default role set karo - ROLE_TEAM_MEMBER
            Set<Role> roles = new HashSet<>();
            Role userRole = roleRepository.findByName(ERole.ROLE_TEAM_MEMBER)
                    .orElseThrow(() -> new ResourceNotFoundException("Role", ERole.ROLE_TEAM_MEMBER.name()));
            roles.add(userRole);
            user.setRoles(roles);

            User savedUser = userRepository.save(user);
            logger.info("User created successfully: {}", savedUser.getUsername());
            return savedUser;

        } catch (DataIntegrityViolationException e) {
            logger.error("Data integrity violation while creating user: {}", registerRequest.getUsername(), e);
            throw new BusinessException("User with this username or email already exists");
        } catch (Exception e) {
            logger.error("Error creating user: {}", registerRequest.getUsername(), e);
            throw new BusinessException("Error creating user: " + e.getMessage());
        }
    }

    public Optional<User> findByUsername(String username) {
        try {
            return userRepository.findByUsername(username);
        } catch (Exception e) {
            logger.error("Error finding user by username: {}", username, e);
            throw new BusinessException("Error retrieving user");
        }
    }

    public Optional<User> findByEmail(String email) {
        try {
            return userRepository.findByEmail(email);
        } catch (Exception e) {
            logger.error("Error finding user by email: {}", email, e);
            throw new BusinessException("Error retrieving user");
        }
    }

    public Optional<User> getUserById(Long id) {
        try {
            Optional<User> user = userRepository.findById(id);
            if (user.isEmpty()) {
                logger.warn("User not found with id: {}", id);
            }
            return user;
        } catch (Exception e) {
            logger.error("Error retrieving user by id: {}", id, e);
            throw new BusinessException("Error retrieving user");
        }
    }

    public List<User> getAllUsers() {
        try {
            return userRepository.findAll();
        } catch (Exception e) {
            logger.error("Error retrieving all users", e);
            throw new BusinessException("Error retrieving users");
        }
    }

    public Page<User> getAllUsers(Pageable pageable) {
        try {
            return userRepository.findAll(pageable);
        } catch (Exception e) {
            logger.error("Error retrieving paginated users", e);
            throw new BusinessException("Error retrieving users");
        }
    }

    public User updateUser(Long userId, UpdateUserRequest updateRequest) {
        try {
            logger.info("Updating user with id: {}", userId);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));

            // Basic information update
            if (updateRequest.getFirstName() != null) {
                user.setFirstName(updateRequest.getFirstName());
            }
            if (updateRequest.getLastName() != null) {
                user.setLastName(updateRequest.getLastName());
            }
            if (updateRequest.getEmail() != null) {
                // Check if email already exists for other users
                Optional<User> existingUser = userRepository.findByEmail(updateRequest.getEmail());
                if (existingUser.isPresent() && !existingUser.get().getId().equals(userId)) {
                    throw new ValidationException("Email is already in use by another user");
                }
                user.setEmail(updateRequest.getEmail());
            }

            // Profile fields
            if (updateRequest.getPhone() != null) {
                user.setPhone(updateRequest.getPhone());
            }
            if (updateRequest.getBio() != null) {
                user.setBio(updateRequest.getBio());
            }

            // Roles update
            if (updateRequest.getRoleIds() != null && !updateRequest.getRoleIds().isEmpty()) {
                Set<Role> roles = new HashSet<>();
                for (Long roleId : updateRequest.getRoleIds()) {
                    Role role = roleRepository.findById(roleId)
                            .orElseThrow(() -> new ResourceNotFoundException("Role", roleId.toString()));
                    roles.add(role);
                }
                user.setRoles(roles);
            }

            // Active status update
            if (updateRequest.getActive() != null) {
                user.setActive(updateRequest.getActive());
            }

            User updatedUser = userRepository.save(user);
            logger.info("User updated successfully: {}", updatedUser.getUsername());
            return updatedUser;

        } catch (DataIntegrityViolationException e) {
            logger.error("Data integrity violation while updating user: {}", userId, e);
            throw new BusinessException("Data integrity violation - possible duplicate email");
        } catch (ResourceNotFoundException e) {
            throw e; // Re-throw specific exceptions
        } catch (Exception e) {
            logger.error("Error updating user: {}", userId, e);
            throw new BusinessException("Error updating user: " + e.getMessage());
        }
    }

    public void deleteUser(Long userId) {
        try {
            logger.info("Deleting user with id: {}", userId);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));

            userRepository.delete(user);
            logger.info("User deleted successfully: {}", userId);

        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting user: {}", userId, e);
            throw new BusinessException("Error deleting user: " + e.getMessage());
        }
    }

    public User toggleUserStatus(Long userId) {
        try {
            logger.info("Toggling status for user: {}", userId);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));

            user.setActive(!user.isActive());
            User updatedUser = userRepository.save(user);

            logger.info("User status toggled to: {} for user: {}", updatedUser.isActive(), userId);
            return updatedUser;

        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error toggling user status: {}", userId, e);
            throw new BusinessException("Error toggling user status");
        }
    }

    public User updateUserRoles(Long userId, Set<Long> roleIds) {
        try {
            logger.info("Updating roles for user: {}", userId);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));

            Set<Role> roles = new HashSet<>();
            for (Long roleId : roleIds) {
                Role role = roleRepository.findById(roleId)
                        .orElseThrow(() -> new ResourceNotFoundException("Role", roleId.toString()));
                roles.add(role);
            }
            user.setRoles(roles);

            User updatedUser = userRepository.save(user);
            logger.info("Roles updated successfully for user: {}", userId);
            return updatedUser;

        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error updating user roles: {}", userId, e);
            throw new BusinessException("Error updating user roles");
        }
    }

    public List<User> searchUsers(String searchTerm) {
        try {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                throw new ValidationException("Search term cannot be empty");
            }

            return userRepository.findByFirstNameContainingOrLastNameContainingOrUsernameContainingOrEmailContaining(
                    searchTerm.trim());
        } catch (Exception e) {
            logger.error("Error searching users with term: {}", searchTerm, e);
            throw new BusinessException("Error searching users");
        }
    }

    public List<User> getUsersByRole(ERole roleName) {
        try {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new ResourceNotFoundException("Role", roleName.name()));
            return userRepository.findByRolesContaining(role);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving users by role: {}", roleName, e);
            throw new BusinessException("Error retrieving users by role");
        }
    }

    public long countUsers() {
        try {
            return userRepository.count();
        } catch (Exception e) {
            logger.error("Error counting users", e);
            throw new BusinessException("Error counting users");
        }
    }

    public long countActiveUsers() {
        try {
            return userRepository.countByActiveTrue();
        } catch (Exception e) {
            logger.error("Error counting active users", e);
            throw new BusinessException("Error counting active users");
        }
    }

    public User updateUserProfile(Long userId, UpdateProfileRequest updateRequest) {
        try {
            logger.info("Updating user profile with id: {}", userId);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));

            // Basic information update
            if (updateRequest.getFirstName() != null) {
                user.setFirstName(updateRequest.getFirstName());
            }
            if (updateRequest.getLastName() != null) {
                user.setLastName(updateRequest.getLastName());
            }
            if (updateRequest.getEmail() != null) {
                // Check if email already exists for other users
                Optional<User> existingUser = userRepository.findByEmail(updateRequest.getEmail());
                if (existingUser.isPresent() && !existingUser.get().getId().equals(userId)) {
                    throw new ValidationException("Email is already in use by another user");
                }
                user.setEmail(updateRequest.getEmail());
            }

            // Profile specific fields
            if (updateRequest.getPhone() != null) {
                user.setPhone(updateRequest.getPhone());
            }
            if (updateRequest.getBio() != null) {
                user.setBio(updateRequest.getBio());
            }

            User updatedUser = userRepository.save(user);
            logger.info("User profile updated successfully: {}", updatedUser.getUsername());
            return updatedUser;

        } catch (DataIntegrityViolationException e) {
            logger.error("Data integrity violation while updating user profile: {}", userId, e);
            throw new BusinessException("Data integrity violation - possible duplicate email");
        } catch (ResourceNotFoundException e) {
            throw e; // Re-throw specific exceptions
        } catch (Exception e) {
            logger.error("Error updating user profile: {}", userId, e);
            throw new BusinessException("Error updating user profile: " + e.getMessage());
        }
    }
}