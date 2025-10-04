package com.nexus_flow.nexus_flow.dto;

import com.nexus_flow.nexus_flow.entity.Project;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CreateProjectRequest {

    @NotBlank(message = "Project name is required")
    private String name;

    private String description;

    @NotBlank(message = "Project code is required")
    private String code;

    private Project.ProjectStatus status = Project.ProjectStatus.PLANNING;

    private Project.Priority priority = Project.Priority.MEDIUM;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate endDate;

    private Double budget;

    private List<Long> teamMemberIds;
}