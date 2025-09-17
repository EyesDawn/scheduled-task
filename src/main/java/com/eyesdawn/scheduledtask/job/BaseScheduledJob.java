package com.eyesdawn.scheduledtask.job;

import com.eyesdawn.scheduledtask.model.TaskExecution;
import com.eyesdawn.scheduledtask.service.TaskExecutionService;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public abstract class BaseScheduledJob implements InterruptableJob {

    private static final Logger logger = LoggerFactory.getLogger(BaseScheduledJob.class);

    @Autowired
    private TaskExecutionService taskExecutionService;

    private volatile boolean interrupted = false;
    private CompletableFuture<Void> executionFuture;

    @Override
    public final void execute(JobExecutionContext context) throws JobExecutionException {
        Long taskId = context.getJobDetail().getJobDataMap().getLong("taskId");
        Long timeoutSeconds = context.getJobDetail().getJobDataMap().getLong("timeoutSeconds");
        
        logger.info("Starting execution of task {} with timeout {} seconds", taskId, timeoutSeconds);

        TaskExecution execution = taskExecutionService.startExecution(taskId);
        
        try {
            // Execute the job with timeout
            executionFuture = CompletableFuture.runAsync(() -> {
                try {
                    String result = executeJob(context);
                    if (!interrupted) {
                        taskExecutionService.completeExecution(execution.getId(), TaskExecution.ExecutionStatus.SUCCESS, result, null);
                        logger.info("Task {} completed successfully", taskId);
                    }
                } catch (Exception e) {
                    if (!interrupted) {
                        logger.error("Task {} failed with error: {}", taskId, e.getMessage(), e);
                        taskExecutionService.completeExecution(execution.getId(), TaskExecution.ExecutionStatus.FAILED, null, e.getMessage());
                    }
                }
            });

            // Apply timeout
            if (timeoutSeconds > 0) {
                executionFuture.get(timeoutSeconds, TimeUnit.SECONDS);
            } else {
                executionFuture.get();
            }

        } catch (java.util.concurrent.TimeoutException e) {
            logger.warn("Task {} timed out after {} seconds", taskId, timeoutSeconds);
            interrupted = true;
            executionFuture.cancel(true);
            taskExecutionService.completeExecution(execution.getId(), TaskExecution.ExecutionStatus.TIMEOUT, null, "Task timed out after " + timeoutSeconds + " seconds");
            
        } catch (Exception e) {
            if (!interrupted) {
                logger.error("Task {} execution failed: {}", taskId, e.getMessage(), e);
                taskExecutionService.completeExecution(execution.getId(), TaskExecution.ExecutionStatus.FAILED, null, e.getMessage());
            }
        }
    }

    @Override
    public void interrupt() throws UnableToInterruptJobException {
        logger.info("Interrupting job execution");
        interrupted = true;
        if (executionFuture != null && !executionFuture.isDone()) {
            executionFuture.cancel(true);
        }
    }

    protected boolean isInterrupted() {
        return interrupted;
    }

    /**
     * Subclasses must implement this method to define their specific job logic
     * @param context The job execution context
     * @return The result of the job execution
     * @throws Exception If the job execution fails
     */
    protected abstract String executeJob(JobExecutionContext context) throws Exception;
}