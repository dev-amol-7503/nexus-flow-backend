package com.nexus_flow.nexus_flow.service;

import com.nexus_flow.nexus_flow.dto.CreateTaskRequest;
import com.nexus_flow.nexus_flow.entity.Task;
import com.nexus_flow.nexus_flow.entity.User;
import com.nexus_flow.nexus_flow.exception.ResourceNotFoundException;
import com.nexus_flow.nexus_flow.repository.TaskRepository;
import com.nexus_flow.nexus_flow.repository.ProjectRepository;
import com.nexus_flow.nexus_flow.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TaskService {

    private static final Logger logger = LoggerFactory.getLogger(TaskService.class);

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Task> getTasksByUser(User user) {
        try {
            return taskRepository.findByUserRelated(user);
        } catch (Exception e) {
            logger.error("Error fetching tasks for user: {}", user.getUsername(), e);
            throw new RuntimeException("Error fetching tasks");
        }
    }

    public List<Task> getTasksByProject(Long projectId) {
        try {
            return taskRepository.findByProjectId(projectId);
        } catch (Exception e) {
            logger.error("Error fetching tasks for project: {}", projectId, e);
            throw new RuntimeException("Error fetching project tasks");
        }
    }

    public Task createTask(CreateTaskRequest createRequest, User reporter) {
        try {
            logger.info("Creating task: {} by reporter: {}", createRequest.getTitle(), reporter.getUsername());

            Task task = new Task();
            task.setTitle(createRequest.getTitle());
            task.setDescription(createRequest.getDescription());
            task.setStatus(createRequest.getStatus());
            task.setPriority(createRequest.getPriority());
            task.setDueDate(createRequest.getDueDate());
            task.setEstimatedHours(createRequest.getEstimatedHours());
            task.setReporter(reporter);

            // Set project
            var project = projectRepository.findById(createRequest.getProjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Project", createRequest.getProjectId().toString()));
            task.setProject(project);

            // Set assignee if provided
            if (createRequest.getAssigneeId() != null) {
                var assignee = userRepository.findById(createRequest.getAssigneeId())
                        .orElseThrow(() -> new ResourceNotFoundException("User", createRequest.getAssigneeId().toString()));
                task.setAssignee(assignee);
            }

            // Set tags if provided
            if (createRequest.getTags() != null) {
                task.setTags(createRequest.getTags());
            }

            Task savedTask = taskRepository.save(task);
            logger.info("Task created successfully: {}", savedTask.getId());
            return savedTask;

        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error creating task: {}", createRequest.getTitle(), e);
            throw new RuntimeException("Error creating task: " + e.getMessage());
        }
    }

    public Task updateTaskStatus(Long taskId, Task.TaskStatus status) {
        try {
            logger.info("Updating task status for task: {} to: {}", taskId, status);

            Task task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new ResourceNotFoundException("Task", taskId.toString()));

            task.setStatus(status);
            Task updatedTask = taskRepository.save(task);
            logger.info("Task status updated successfully: {}", updatedTask.getId());
            return updatedTask;

        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error updating task status for task: {}", taskId, e);
            throw new RuntimeException("Error updating task status");
        }
    }

    public Task updateTask(Long taskId, CreateTaskRequest updateRequest) {
        try {
            logger.info("Updating task with id: {}", taskId);

            Task task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new ResourceNotFoundException("Task", taskId.toString()));

            if (updateRequest.getTitle() != null) {
                task.setTitle(updateRequest.getTitle());
            }
            if (updateRequest.getDescription() != null) {
                task.setDescription(updateRequest.getDescription());
            }
            if (updateRequest.getStatus() != null) {
                task.setStatus(updateRequest.getStatus());
            }
            if (updateRequest.getPriority() != null) {
                task.setPriority(updateRequest.getPriority());
            }
            if (updateRequest.getDueDate() != null) {
                task.setDueDate(updateRequest.getDueDate());
            }
            if (updateRequest.getEstimatedHours() != null) {
                task.setEstimatedHours(updateRequest.getEstimatedHours());
            }

            Task updatedTask = taskRepository.save(task);
            logger.info("Task updated successfully: {}", updatedTask.getId());
            return updatedTask;

        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error updating task with id: {}", taskId, e);
            throw new RuntimeException("Error updating task: " + e.getMessage());
        }
    }

    public void deleteTask(Long taskId) {
        try {
            logger.info("Deleting task with id: {}", taskId);

            Task task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new ResourceNotFoundException("Task", taskId.toString()));

            taskRepository.delete(task);
            logger.info("Task deleted successfully: {}", taskId);

        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting task with id: {}", taskId, e);
            throw new RuntimeException("Error deleting task");
        }
    }

    public Optional<Task> getTaskById(Long id) {
        try {
            return taskRepository.findById(id);
        } catch (Exception e) {
            logger.error("Error fetching task with id: {}", id, e);
            throw new RuntimeException("Error fetching task");
        }
    }
}