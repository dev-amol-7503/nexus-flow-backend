package com.nexus_flow.nexus_flow.service;

import com.nexus_flow.nexus_flow.dto.CreateProjectRequest;
import com.nexus_flow.nexus_flow.entity.Project;
import com.nexus_flow.nexus_flow.entity.User;
import com.nexus_flow.nexus_flow.exception.ResourceNotFoundException;
import com.nexus_flow.nexus_flow.repository.ProjectRepository;
import com.nexus_flow.nexus_flow.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class ProjectService {

    private static final Logger logger = LoggerFactory.getLogger(ProjectService.class);

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    public Page<Project> getProjectsByUser(User user, Pageable pageable) {
        try {
            return projectRepository.findByUserInvolved(user, pageable);
        } catch (Exception e) {
            logger.error("Error fetching projects for user: {}", user.getUsername(), e);
            throw new RuntimeException("Error fetching projects");
        }
    }

    public Optional<Project> getProjectById(Long id) {
        try {
            return projectRepository.findById(id);
        } catch (Exception e) {
            logger.error("Error fetching project with id: {}", id, e);
            throw new RuntimeException("Error fetching project");
        }
    }

    public Project createProject(CreateProjectRequest createRequest, User owner) {
        try {
            logger.info("Creating project: {} for owner: {}", createRequest.getName(), owner.getUsername());

            Project project = new Project();
            project.setName(createRequest.getName());
            project.setDescription(createRequest.getDescription());
            project.setCode(createRequest.getCode());
            project.setStatus(createRequest.getStatus());
            project.setPriority(createRequest.getPriority());
            project.setStartDate(createRequest.getStartDate());
            project.setEndDate(createRequest.getEndDate());
            project.setBudget(createRequest.getBudget());
            project.setOwner(owner);

            // Add team members if provided
            if (createRequest.getTeamMemberIds() != null && !createRequest.getTeamMemberIds().isEmpty()) {
                List<User> teamMembers = new ArrayList<>();
                for (Long userId : createRequest.getTeamMemberIds()) {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));
                    teamMembers.add(user);
                }
                project.setTeamMembers(teamMembers);
            }

            Project savedProject = projectRepository.save(project);
            logger.info("Project created successfully: {}", savedProject.getId());
            return savedProject;

        } catch (Exception e) {
            logger.error("Error creating project: {}", createRequest.getName(), e);
            throw new RuntimeException("Error creating project: " + e.getMessage());
        }
    }

    public Project updateProject(Long projectId, CreateProjectRequest updateRequest) {
        try {
            logger.info("Updating project with id: {}", projectId);

            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new ResourceNotFoundException("Project", projectId.toString()));

            if (updateRequest.getName() != null) {
                project.setName(updateRequest.getName());
            }
            if (updateRequest.getDescription() != null) {
                project.setDescription(updateRequest.getDescription());
            }
            if (updateRequest.getStatus() != null) {
                project.setStatus(updateRequest.getStatus());
            }
            if (updateRequest.getPriority() != null) {
                project.setPriority(updateRequest.getPriority());
            }
            if (updateRequest.getStartDate() != null) {
                project.setStartDate(updateRequest.getStartDate());
            }
            if (updateRequest.getEndDate() != null) {
                project.setEndDate(updateRequest.getEndDate());
            }
            if (updateRequest.getBudget() != null) {
                project.setBudget(updateRequest.getBudget());
            }

            Project updatedProject = projectRepository.save(project);
            logger.info("Project updated successfully: {}", updatedProject.getId());
            return updatedProject;

        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error updating project with id: {}", projectId, e);
            throw new RuntimeException("Error updating project: " + e.getMessage());
        }
    }

    public void deleteProject(Long projectId) {
        try {
            logger.info("Deleting project with id: {}", projectId);

            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new ResourceNotFoundException("Project", projectId.toString()));

            projectRepository.delete(project);
            logger.info("Project deleted successfully: {}", projectId);

        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting project with id: {}", projectId, e);
            throw new RuntimeException("Error deleting project");
        }
    }

    public Object getProjectStatistics() {
        try {
            Map<String, Object> stats = new HashMap<>();

            // Count projects by status
            for (Project.ProjectStatus status : Project.ProjectStatus.values()) {
                long count = projectRepository.countByStatus(status);
                stats.put(status.name().toLowerCase() + "Projects", count);
            }

            // Total projects
            stats.put("totalProjects", projectRepository.count());

            return stats;
        } catch (Exception e) {
            logger.error("Error fetching project statistics", e);
            throw new RuntimeException("Error fetching project statistics");
        }
    }
}