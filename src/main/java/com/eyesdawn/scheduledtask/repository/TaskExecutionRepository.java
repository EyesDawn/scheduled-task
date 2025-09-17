package com.eyesdawn.scheduledtask.repository;

import com.eyesdawn.scheduledtask.model.TaskExecution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskExecutionRepository extends JpaRepository<TaskExecution, Long> {

    List<TaskExecution> findByTaskIdOrderByStartTimeDesc(Long taskId);

    Page<TaskExecution> findByTaskIdOrderByStartTimeDesc(Long taskId, Pageable pageable);

    List<TaskExecution> findByStatus(TaskExecution.ExecutionStatus status);

    @Query("SELECT e FROM TaskExecution e WHERE e.task.id = ?1 AND e.status = ?2 ORDER BY e.startTime DESC")
    List<TaskExecution> findByTaskIdAndStatus(Long taskId, TaskExecution.ExecutionStatus status);

    @Query("SELECT e FROM TaskExecution e WHERE e.startTime >= ?1 AND e.startTime <= ?2 ORDER BY e.startTime DESC")
    List<TaskExecution> findExecutionsBetween(LocalDateTime startTime, LocalDateTime endTime);

    @Query("SELECT COUNT(e) FROM TaskExecution e WHERE e.task.id = ?1 AND e.status = 'SUCCESS'")
    long countSuccessfulExecutions(Long taskId);

    @Query("SELECT COUNT(e) FROM TaskExecution e WHERE e.task.id = ?1 AND e.status = 'FAILED'")
    long countFailedExecutions(Long taskId);
}