package com.eyesdawn.scheduledtask.repository;

import com.eyesdawn.scheduledtask.model.TaskDependency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskDependencyRepository extends JpaRepository<TaskDependency, Long> {

    List<TaskDependency> findByTaskIdAndEnabled(Long taskId, Boolean enabled);

    List<TaskDependency> findByDependentTaskIdAndEnabled(Long dependentTaskId, Boolean enabled);

    @Query("SELECT d FROM TaskDependency d WHERE d.task.id = ?1 AND d.enabled = true")
    List<TaskDependency> findActiveDependenciesForTask(Long taskId);

    @Query("SELECT d FROM TaskDependency d WHERE d.dependentTask.id = ?1 AND d.enabled = true")
    List<TaskDependency> findTasksDependingOn(Long dependentTaskId);

    boolean existsByTaskIdAndDependentTaskIdAndEnabled(Long taskId, Long dependentTaskId, Boolean enabled);
}