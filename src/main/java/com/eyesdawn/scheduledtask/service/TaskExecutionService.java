package com.eyesdawn.scheduledtask.service;

import com.eyesdawn.scheduledtask.model.ScheduledTask;
import com.eyesdawn.scheduledtask.model.TaskExecution;
import com.eyesdawn.scheduledtask.repository.ScheduledTaskRepository;
import com.eyesdawn.scheduledtask.repository.TaskExecutionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class TaskExecutionService {

    private static final Logger logger = LoggerFactory.getLogger(TaskExecutionService.class);

    @Autowired
    private TaskExecutionRepository taskExecutionRepository;

    @Autowired
    private ScheduledTaskRepository scheduledTaskRepository;

    public TaskExecution startExecution(Long taskId) {
        ScheduledTask task = scheduledTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));

        TaskExecution execution = new TaskExecution(task);
        execution.setStartTime(LocalDateTime.now());
        execution.setStatus(TaskExecution.ExecutionStatus.RUNNING);
        
        return taskExecutionRepository.save(execution);
    }

    public TaskExecution completeExecution(Long executionId, TaskExecution.ExecutionStatus status, 
                                         String result, String errorMessage) {
        TaskExecution execution = taskExecutionRepository.findById(executionId)
                .orElseThrow(() -> new RuntimeException("Execution not found: " + executionId));

        execution.setEndTime(LocalDateTime.now());
        execution.setStatus(status);
        execution.setResult(result);
        execution.setErrorMessage(errorMessage);

        if (execution.getStartTime() != null) {
            long duration = java.time.Duration.between(execution.getStartTime(), execution.getEndTime()).toMillis();
            execution.setDurationMillis(duration);
        }

        TaskExecution savedExecution = taskExecutionRepository.save(execution);
        
        // Handle retry logic if task failed
        if (status == TaskExecution.ExecutionStatus.FAILED) {
            handleRetryLogic(savedExecution);
        }

        return savedExecution;
    }

    private void handleRetryLogic(TaskExecution failedExecution) {
        ScheduledTask task = failedExecution.getTask();
        
        if (failedExecution.getRetryAttempt() < task.getMaxRetries()) {
            logger.info("Scheduling retry for task {} (attempt {}/{})", 
                    task.getId(), failedExecution.getRetryAttempt() + 1, task.getMaxRetries());
            
            // Create a new execution for retry
            TaskExecution retryExecution = new TaskExecution(task);
            retryExecution.setRetryAttempt(failedExecution.getRetryAttempt() + 1);
            retryExecution.setStatus(TaskExecution.ExecutionStatus.RETRY_SCHEDULED);
            taskExecutionRepository.save(retryExecution);
            
            // In a real implementation, you would schedule the retry with a delay
            // For now, we'll just mark it as scheduled
        } else {
            logger.warn("Task {} has exceeded maximum retry attempts ({})", task.getId(), task.getMaxRetries());
            task.setStatus(ScheduledTask.TaskStatus.ERROR);
            scheduledTaskRepository.save(task);
        }
    }

    @Transactional(readOnly = true)
    public List<TaskExecution> getExecutionsForTask(Long taskId) {
        return taskExecutionRepository.findByTaskIdOrderByStartTimeDesc(taskId);
    }

    @Transactional(readOnly = true)
    public Page<TaskExecution> getExecutionsForTask(Long taskId, Pageable pageable) {
        return taskExecutionRepository.findByTaskIdOrderByStartTimeDesc(taskId, pageable);
    }

    @Transactional(readOnly = true)
    public List<TaskExecution> getRunningExecutions() {
        return taskExecutionRepository.findByStatus(TaskExecution.ExecutionStatus.RUNNING);
    }

    @Transactional(readOnly = true)
    public TaskExecution getExecution(Long executionId) {
        return taskExecutionRepository.findById(executionId)
                .orElseThrow(() -> new RuntimeException("Execution not found: " + executionId));
    }
}