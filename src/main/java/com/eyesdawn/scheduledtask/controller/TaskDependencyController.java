package com.eyesdawn.scheduledtask.controller;

import com.eyesdawn.scheduledtask.model.TaskDependency;
import com.eyesdawn.scheduledtask.service.TaskDependencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dependencies")
public class TaskDependencyController {

    @Autowired
    private TaskDependencyService taskDependencyService;

    @PostMapping
    public ResponseEntity<TaskDependency> addDependency(@RequestParam Long taskId, @RequestParam Long dependentTaskId) {
        try {
            TaskDependency dependency = taskDependencyService.addDependency(taskId, dependentTaskId);
            return ResponseEntity.ok(dependency);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeDependency(@PathVariable Long id) {
        try {
            taskDependencyService.removeDependency(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<TaskDependency>> getDependenciesForTask(@PathVariable Long taskId) {
        List<TaskDependency> dependencies = taskDependencyService.getDependenciesForTask(taskId);
        return ResponseEntity.ok(dependencies);
    }

    @GetMapping("/dependents/{taskId}")
    public ResponseEntity<List<TaskDependency>> getTasksDependingOn(@PathVariable Long taskId) {
        List<TaskDependency> dependents = taskDependencyService.getTasksDependingOn(taskId);
        return ResponseEntity.ok(dependents);
    }
}