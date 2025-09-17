package com.eyesdawn.scheduledtask.service;

import com.eyesdawn.scheduledtask.model.ScheduledTask;
import com.eyesdawn.scheduledtask.model.TaskDependency;
import com.eyesdawn.scheduledtask.repository.ScheduledTaskRepository;
import com.eyesdawn.scheduledtask.repository.TaskDependencyRepository;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ScheduledTaskService {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTaskService.class);

    @Autowired
    private ScheduledTaskRepository scheduledTaskRepository;

    @Autowired
    private TaskDependencyRepository taskDependencyRepository;

    @Autowired
    private Scheduler quartzScheduler;

    public ScheduledTask createTask(ScheduledTask task) {
        validateCronExpression(task.getCronExpression());
        validateJobClass(task.getJobClass());
        
        ScheduledTask savedTask = scheduledTaskRepository.save(task);
        logger.info("Created new scheduled task: {}", savedTask.getName());
        
        return savedTask;
    }

    public ScheduledTask updateTask(Long taskId, ScheduledTask updatedTask) {
        ScheduledTask existingTask = scheduledTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));

        // Stop existing scheduled job if active
        if (existingTask.getStatus() == ScheduledTask.TaskStatus.ACTIVE) {
            stopTask(taskId);
        }

        // Update task properties
        existingTask.setName(updatedTask.getName());
        existingTask.setDescription(updatedTask.getDescription());
        existingTask.setCronExpression(updatedTask.getCronExpression());
        existingTask.setJobClass(updatedTask.getJobClass());
        existingTask.setJobData(updatedTask.getJobData());
        existingTask.setMaxRetries(updatedTask.getMaxRetries());
        existingTask.setTimeoutSeconds(updatedTask.getTimeoutSeconds());

        validateCronExpression(existingTask.getCronExpression());
        validateJobClass(existingTask.getJobClass());

        ScheduledTask savedTask = scheduledTaskRepository.save(existingTask);
        logger.info("Updated scheduled task: {}", savedTask.getName());

        return savedTask;
    }

    public void deleteTask(Long taskId) {
        ScheduledTask task = scheduledTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));

        // Stop the task if it's running
        if (task.getStatus() == ScheduledTask.TaskStatus.ACTIVE) {
            stopTask(taskId);
        }

        scheduledTaskRepository.delete(task);
        logger.info("Deleted scheduled task: {}", task.getName());
    }

    public ScheduledTask startTask(Long taskId) {
        ScheduledTask task = scheduledTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));

        if (task.getStatus() == ScheduledTask.TaskStatus.ACTIVE) {
            throw new RuntimeException("Task is already active: " + task.getName());
        }

        // Check dependencies before starting
        if (!areDependenciesSatisfied(taskId)) {
            throw new RuntimeException("Task dependencies are not satisfied: " + task.getName());
        }

        try {
            scheduleQuartzJob(task);
            task.setStatus(ScheduledTask.TaskStatus.ACTIVE);
            ScheduledTask savedTask = scheduledTaskRepository.save(task);
            
            logger.info("Started scheduled task: {}", task.getName());
            return savedTask;
            
        } catch (SchedulerException e) {
            logger.error("Failed to start scheduled task: {}", task.getName(), e);
            throw new RuntimeException("Failed to start task: " + e.getMessage(), e);
        }
    }

    public ScheduledTask stopTask(Long taskId) {
        ScheduledTask task = scheduledTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));

        if (task.getStatus() != ScheduledTask.TaskStatus.ACTIVE) {
            throw new RuntimeException("Task is not active: " + task.getName());
        }

        try {
            JobKey jobKey = new JobKey(task.getName(), "DEFAULT");
            quartzScheduler.deleteJob(jobKey);
            
            task.setStatus(ScheduledTask.TaskStatus.INACTIVE);
            ScheduledTask savedTask = scheduledTaskRepository.save(task);
            
            logger.info("Stopped scheduled task: {}", task.getName());
            return savedTask;
            
        } catch (SchedulerException e) {
            logger.error("Failed to stop scheduled task: {}", task.getName(), e);
            throw new RuntimeException("Failed to stop task: " + e.getMessage(), e);
        }
    }

    public ScheduledTask pauseTask(Long taskId) {
        ScheduledTask task = scheduledTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));

        if (task.getStatus() != ScheduledTask.TaskStatus.ACTIVE) {
            throw new RuntimeException("Task is not active: " + task.getName());
        }

        try {
            JobKey jobKey = new JobKey(task.getName(), "DEFAULT");
            quartzScheduler.pauseJob(jobKey);
            
            task.setStatus(ScheduledTask.TaskStatus.PAUSED);
            ScheduledTask savedTask = scheduledTaskRepository.save(task);
            
            logger.info("Paused scheduled task: {}", task.getName());
            return savedTask;
            
        } catch (SchedulerException e) {
            logger.error("Failed to pause scheduled task: {}", task.getName(), e);
            throw new RuntimeException("Failed to pause task: " + e.getMessage(), e);
        }
    }

    public ScheduledTask resumeTask(Long taskId) {
        ScheduledTask task = scheduledTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));

        if (task.getStatus() != ScheduledTask.TaskStatus.PAUSED) {
            throw new RuntimeException("Task is not paused: " + task.getName());
        }

        try {
            JobKey jobKey = new JobKey(task.getName(), "DEFAULT");
            quartzScheduler.resumeJob(jobKey);
            
            task.setStatus(ScheduledTask.TaskStatus.ACTIVE);
            ScheduledTask savedTask = scheduledTaskRepository.save(task);
            
            logger.info("Resumed scheduled task: {}", task.getName());
            return savedTask;
            
        } catch (SchedulerException e) {
            logger.error("Failed to resume scheduled task: {}", task.getName(), e);
            throw new RuntimeException("Failed to resume task: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<ScheduledTask> getAllTasks() {
        return scheduledTaskRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<ScheduledTask> getTask(Long taskId) {
        return scheduledTaskRepository.findById(taskId);
    }

    @Transactional(readOnly = true)
    public List<ScheduledTask> getActiveTasks() {
        return scheduledTaskRepository.findActiveTasks();
    }

    private void scheduleQuartzJob(ScheduledTask task) throws SchedulerException {
        Class<? extends Job> jobClass = getJobClass(task.getJobClass());
        
        JobDetail jobDetail = JobBuilder.newJob(jobClass)
                .withIdentity(task.getName(), "DEFAULT")
                .usingJobData("taskId", task.getId())
                .usingJobData("timeoutSeconds", task.getTimeoutSeconds())
                .usingJobData("jobData", task.getJobData())
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(task.getName() + "_trigger", "DEFAULT")
                .withSchedule(CronScheduleBuilder.cronSchedule(task.getCronExpression()))
                .build();

        quartzScheduler.scheduleJob(jobDetail, trigger);
    }

    private boolean areDependenciesSatisfied(Long taskId) {
        List<TaskDependency> dependencies = taskDependencyRepository.findActiveDependenciesForTask(taskId);
        
        for (TaskDependency dependency : dependencies) {
            ScheduledTask dependentTask = dependency.getDependentTask();
            if (dependentTask.getStatus() != ScheduledTask.TaskStatus.ACTIVE) {
                logger.warn("Dependency not satisfied: Task {} depends on inactive task {}", 
                        taskId, dependentTask.getName());
                return false;
            }
        }
        
        return true;
    }

    private void validateCronExpression(String cronExpression) {
        try {
            CronExpression.validateExpression(cronExpression);
        } catch (Exception e) {
            throw new RuntimeException("Invalid cron expression: " + cronExpression, e);
        }
    }

    @SuppressWarnings("unchecked")
    private Class<? extends Job> getJobClass(String jobClassName) {
        try {
            return (Class<? extends Job>) Class.forName(jobClassName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Job class not found: " + jobClassName, e);
        }
    }

    private void validateJobClass(String jobClassName) {
        getJobClass(jobClassName); // This will throw if invalid
    }
}