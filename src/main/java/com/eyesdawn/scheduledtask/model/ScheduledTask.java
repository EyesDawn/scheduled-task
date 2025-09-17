package com.eyesdawn.scheduledtask.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "scheduled_tasks")
@EntityListeners(AuditingEntityListener.class)
public class ScheduledTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String name;

    @Column(length = 500)
    private String description;

    @NotBlank
    @Column(nullable = false)
    private String cronExpression;

    @NotBlank
    @Column(nullable = false)
    private String jobClass;

    @Column(length = 1000)
    private String jobData; // JSON format for job parameters

    @NotNull
    @Enumerated(EnumType.STRING)
    private TaskStatus status = TaskStatus.INACTIVE;

    @Column(nullable = false)
    private Integer retryCount = 0;

    @Column(nullable = false)
    private Integer maxRetries = 3;

    @Column(nullable = false)
    private Long timeoutSeconds = 300L; // 5 minutes default

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TaskExecution> executions = new ArrayList<>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TaskDependency> dependencies = new ArrayList<>();

    @OneToMany(mappedBy = "dependentTask", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TaskDependency> dependents = new ArrayList<>();

    // Constructors
    public ScheduledTask() {}

    public ScheduledTask(String name, String description, String cronExpression, String jobClass) {
        this.name = name;
        this.description = description;
        this.cronExpression = cronExpression;
        this.jobClass = jobClass;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public String getJobClass() {
        return jobClass;
    }

    public void setJobClass(String jobClass) {
        this.jobClass = jobClass;
    }

    public String getJobData() {
        return jobData;
    }

    public void setJobData(String jobData) {
        this.jobData = jobData;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public Integer getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }

    public Long getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(Long timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<TaskExecution> getExecutions() {
        return executions;
    }

    public void setExecutions(List<TaskExecution> executions) {
        this.executions = executions;
    }

    public List<TaskDependency> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<TaskDependency> dependencies) {
        this.dependencies = dependencies;
    }

    public List<TaskDependency> getDependents() {
        return dependents;
    }

    public void setDependents(List<TaskDependency> dependents) {
        this.dependents = dependents;
    }

    public enum TaskStatus {
        ACTIVE,    // Task is scheduled and running
        INACTIVE,  // Task is not scheduled
        PAUSED,    // Task is temporarily paused
        ERROR      // Task has encountered an error
    }
}