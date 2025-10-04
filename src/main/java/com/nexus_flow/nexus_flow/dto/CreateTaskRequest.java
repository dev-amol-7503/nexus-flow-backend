package com.nexus_flow.nexus_flow.dto;

import com.nexus_flow.nexus_flow.entity.Task;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CreateTaskRequest {

    @NotBlank(message = "Task title is required")
    private String title;

    private String description;

    private Task.TaskStatus status = Task.TaskStatus.TODO;

    private Task.TaskPriority priority = Task.TaskPriority.MEDIUM;

    private LocalDate dueDate;

    private Integer estimatedHours;

    @NotNull(message = "Project ID is required")
    private Long projectId;

    private Long assigneeId;

    private List<String> tags;
}