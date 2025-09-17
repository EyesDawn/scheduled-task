package com.eyesdawn.scheduledtask.repository;

import com.eyesdawn.scheduledtask.model.ScheduledTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduledTaskRepository extends JpaRepository<ScheduledTask, Long> {

    Optional<ScheduledTask> findByName(String name);

    List<ScheduledTask> findByStatus(ScheduledTask.TaskStatus status);

    @Query("SELECT t FROM ScheduledTask t WHERE t.status = 'ACTIVE'")
    List<ScheduledTask> findActiveTasks();

    @Query("SELECT t FROM ScheduledTask t LEFT JOIN FETCH t.dependencies WHERE t.id = ?1")
    Optional<ScheduledTask> findByIdWithDependencies(Long id);

    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM TaskDependency d WHERE d.task.id = ?1")
    boolean hasDependencies(Long taskId);
}