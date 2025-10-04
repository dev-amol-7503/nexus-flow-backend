package com.nexus_flow.nexus_flow.repository;

import com.nexus_flow.nexus_flow.entity.Project;
import com.nexus_flow.nexus_flow.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    Page<Project> findByOwner(User owner, Pageable pageable);
    List<Project> findByTeamMembersContaining(User user);

    @Query("SELECT p FROM Project p WHERE p.owner = :user OR :user MEMBER OF p.teamMembers")
    Page<Project> findByUserInvolved(@Param("user") User user, Pageable pageable);

    long countByStatus(Project.ProjectStatus status);
}