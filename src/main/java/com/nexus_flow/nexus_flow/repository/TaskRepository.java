package com.nexus_flow.nexus_flow.repository;

import com.nexus_flow.nexus_flow.entity.Task;
import com.nexus_flow.nexus_flow.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByProjectId(Long projectId);
    List<Task> findByAssignee(User assignee);
    List<Task> findByReporter(User reporter);
    List<Task> findByStatus(Task.TaskStatus status);

    @Query("SELECT t FROM Task t WHERE t.assignee = :user OR t.reporter = :user")
    List<Task> findByUserRelated(@Param("user") User user);

    long countByStatus(Task.TaskStatus status);
}
