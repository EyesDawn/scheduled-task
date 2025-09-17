package com.eyesdawn.scheduledtask.service;

import com.eyesdawn.scheduledtask.model.ScheduledTask;
import com.eyesdawn.scheduledtask.model.TaskDependency;
import com.eyesdawn.scheduledtask.repository.ScheduledTaskRepository;
import com.eyesdawn.scheduledtask.repository.TaskDependencyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TaskDependencyService {

    private static final Logger logger = LoggerFactory.getLogger(TaskDependencyService.class);

    @Autowired
    private TaskDependencyRepository taskDependencyRepository;

    @Autowired
    private ScheduledTaskRepository scheduledTaskRepository;

    public TaskDependency addDependency(Long taskId, Long dependentTaskId) {
        if (taskId.equals(dependentTaskId)) {
            throw new RuntimeException("A task cannot depend on itself");
        }

        ScheduledTask task = scheduledTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));

        ScheduledTask dependentTask = scheduledTaskRepository.findById(dependentTaskId)
                .orElseThrow(() -> new RuntimeException("Dependent task not found: " + dependentTaskId));

        // Check if dependency already exists
        if (taskDependencyRepository.existsByTaskIdAndDependentTaskIdAndEnabled(taskId, dependentTaskId, true)) {
            throw new RuntimeException("Dependency already exists between tasks");
        }

        // Check for circular dependencies
        if (wouldCreateCircularDependency(taskId, dependentTaskId)) {
            throw new RuntimeException("Adding this dependency would create a circular dependency");
        }

        TaskDependency dependency = new TaskDependency(task, dependentTask);
        TaskDependency savedDependency = taskDependencyRepository.save(dependency);

        logger.info("Added dependency: Task {} depends on Task {}", task.getName(), dependentTask.getName());
        return savedDependency;
    }

    public void removeDependency(Long dependencyId) {
        TaskDependency dependency = taskDependencyRepository.findById(dependencyId)
                .orElseThrow(() -> new RuntimeException("Dependency not found: " + dependencyId));

        dependency.setEnabled(false);
        taskDependencyRepository.save(dependency);

        logger.info("Removed dependency: Task {} no longer depends on Task {}", 
                dependency.getTask().getName(), dependency.getDependentTask().getName());
    }

    @Transactional(readOnly = true)
    public List<TaskDependency> getDependenciesForTask(Long taskId) {
        return taskDependencyRepository.findActiveDependenciesForTask(taskId);
    }

    @Transactional(readOnly = true)
    public List<TaskDependency> getTasksDependingOn(Long taskId) {
        return taskDependencyRepository.findTasksDependingOn(taskId);
    }

    /**
     * Check if adding a dependency would create a circular dependency
     */
    private boolean wouldCreateCircularDependency(Long taskId, Long dependentTaskId) {
        return hasPath(dependentTaskId, taskId);
    }

    /**
     * Recursively check if there's a path from startTaskId to endTaskId
     */
    private boolean hasPath(Long startTaskId, Long endTaskId) {
        if (startTaskId.equals(endTaskId)) {
            return true;
        }

        List<TaskDependency> dependencies = taskDependencyRepository.findActiveDependenciesForTask(startTaskId);
        
        for (TaskDependency dependency : dependencies) {
            if (hasPath(dependency.getDependentTask().getId(), endTaskId)) {
                return true;
            }
        }

        return false;
    }
}