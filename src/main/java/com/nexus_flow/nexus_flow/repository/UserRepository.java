package com.nexus_flow.nexus_flow.repository;

import com.nexus_flow.nexus_flow.entity.User;
import com.nexus_flow.nexus_flow.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.username = :username OR u.email = :email")
    Optional<User> findByUsernameOrEmail(@Param("username") String username, @Param("email") String email);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);

    // Search users by various fields
    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<User> findByFirstNameContainingOrLastNameContainingOrUsernameContainingOrEmailContaining(
            @Param("searchTerm") String searchTerm);

    // Find users by role
    List<User> findByRolesContaining(Role role);

    // Count active users
    @Query("SELECT COUNT(u) FROM User u WHERE u.active = true")
    long countByActiveTrue();

    // Find all active users
    List<User> findByActiveTrue();

    // Find all inactive users
    List<User> findByActiveFalse();
}