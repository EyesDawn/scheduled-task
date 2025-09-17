package com.eyesdawn.scheduledtask.model;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "task_dependencies")
@EntityListeners(AuditingEntityListener.class)
public class TaskDependency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private ScheduledTask task;  // The task that depends on another

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dependent_task_id", nullable = false)
    private ScheduledTask dependentTask;  // The task that must complete first

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private Boolean enabled = true;

    // Constructors
    public TaskDependency() {}

    public TaskDependency(ScheduledTask task, ScheduledTask dependentTask) {
        this.task = task;
        this.dependentTask = dependentTask;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ScheduledTask getTask() {
        return task;
    }

    public void setTask(ScheduledTask task) {
        this.task = task;
    }

    public ScheduledTask getDependentTask() {
        return dependentTask;
    }

    public void setDependentTask(ScheduledTask dependentTask) {
        this.dependentTask = dependentTask;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}