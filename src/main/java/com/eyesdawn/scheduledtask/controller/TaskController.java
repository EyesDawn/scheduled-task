package com.eyesdawn.scheduledtask.controller;

import com.eyesdawn.scheduledtask.model.ScheduledTask;
import com.eyesdawn.scheduledtask.model.TaskExecution;
import com.eyesdawn.scheduledtask.service.ScheduledTaskService;
import com.eyesdawn.scheduledtask.service.TaskExecutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private ScheduledTaskService scheduledTaskService;

    @Autowired
    private TaskExecutionService taskExecutionService;

    @GetMapping
    public ResponseEntity<List<ScheduledTask>> getAllTasks() {
        List<ScheduledTask> tasks = scheduledTaskService.getAllTasks();
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScheduledTask> getTask(@PathVariable Long id) {
        Optional<ScheduledTask> task = scheduledTaskService.getTask(id);
        return task.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/active")
    public ResponseEntity<List<ScheduledTask>> getActiveTasks() {
        List<ScheduledTask> tasks = scheduledTaskService.getActiveTasks();
        return ResponseEntity.ok(tasks);
    }

    @PostMapping
    public ResponseEntity<ScheduledTask> createTask(@RequestBody ScheduledTask task) {
        try {
            ScheduledTask createdTask = scheduledTaskService.createTask(task);
            return ResponseEntity.ok(createdTask);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ScheduledTask> updateTask(@PathVariable Long id, @RequestBody ScheduledTask task) {
        try {
            ScheduledTask updatedTask = scheduledTaskService.updateTask(id, task);
            return ResponseEntity.ok(updatedTask);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        try {
            scheduledTaskService.deleteTask(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<ScheduledTask> startTask(@PathVariable Long id) {
        try {
            ScheduledTask task = scheduledTaskService.startTask(id);
            return ResponseEntity.ok(task);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/stop")
    public ResponseEntity<ScheduledTask> stopTask(@PathVariable Long id) {
        try {
            ScheduledTask task = scheduledTaskService.stopTask(id);
            return ResponseEntity.ok(task);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/pause")
    public ResponseEntity<ScheduledTask> pauseTask(@PathVariable Long id) {
        try {
            ScheduledTask task = scheduledTaskService.pauseTask(id);
            return ResponseEntity.ok(task);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/resume")
    public ResponseEntity<ScheduledTask> resumeTask(@PathVariable Long id) {
        try {
            ScheduledTask task = scheduledTaskService.resumeTask(id);
            return ResponseEntity.ok(task);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}/executions")
    public ResponseEntity<Page<TaskExecution>> getTaskExecutions(@PathVariable Long id, Pageable pageable) {
        Page<TaskExecution> executions = taskExecutionService.getExecutionsForTask(id, pageable);
        return ResponseEntity.ok(executions);
    }

    @GetMapping("/executions/running")
    public ResponseEntity<List<TaskExecution>> getRunningExecutions() {
        List<TaskExecution> executions = taskExecutionService.getRunningExecutions();
        return ResponseEntity.ok(executions);
    }
}