package com.nexus_flow.nexus_flow.service;

import com.nexus_flow.nexus_flow.entity.Task;
import com.nexus_flow.nexus_flow.repository.ProjectRepository;
import com.nexus_flow.nexus_flow.repository.TaskRepository;
import com.nexus_flow.nexus_flow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class DashboardService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // Total projects count
        long totalProjects = projectRepository.count();
        stats.put("totalProjects", totalProjects);

        // Completed tasks count
        long completedTasks = taskRepository.countByStatus(Task.TaskStatus.DONE);
        stats.put("completedTasks", completedTasks);

        // Pending tasks count (all statuses except DONE)
        long pendingTasks = taskRepository.count() - completedTasks;
        stats.put("pendingTasks", pendingTasks);

        // Team members count (active users)
        long teamMembers = userRepository.countByActiveTrue();
        stats.put("teamMembers", teamMembers);

        return stats;
    }
}