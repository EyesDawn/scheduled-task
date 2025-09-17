package com.eyesdawn.scheduledtask.controller;

import com.eyesdawn.scheduledtask.model.ScheduledTask;
import com.eyesdawn.scheduledtask.model.TaskExecution;
import com.eyesdawn.scheduledtask.service.ScheduledTaskService;
import com.eyesdawn.scheduledtask.service.TaskExecutionService;
import com.eyesdawn.scheduledtask.service.TaskDependencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/web")
public class WebController {

    @Autowired
    private ScheduledTaskService scheduledTaskService;

    @Autowired
    private TaskExecutionService taskExecutionService;

    @Autowired
    private TaskDependencyService taskDependencyService;

    @GetMapping({"", "/"})
    public String index() {
        return "redirect:/web/tasks";
    }

    @GetMapping("/tasks")
    public String listTasks(Model model) {
        List<ScheduledTask> tasks = scheduledTaskService.getAllTasks();
        model.addAttribute("tasks", tasks);
        return "tasks/list";
    }

    @GetMapping("/tasks/new")
    public String newTask(Model model) {
        model.addAttribute("task", new ScheduledTask());
        return "tasks/form";
    }

    @GetMapping("/tasks/{id}")
    public String viewTask(@PathVariable Long id, Model model) {
        Optional<ScheduledTask> task = scheduledTaskService.getTask(id);
        if (task.isEmpty()) {
            return "redirect:/web/tasks";
        }

        // Get recent executions
        Pageable pageable = PageRequest.of(0, 10);
        model.addAttribute("task", task.get());
        model.addAttribute("executions", taskExecutionService.getExecutionsForTask(id, pageable));
        model.addAttribute("dependencies", taskDependencyService.getDependenciesForTask(id));
        model.addAttribute("dependents", taskDependencyService.getTasksDependingOn(id));

        return "tasks/detail";
    }

    @GetMapping("/tasks/{id}/edit")
    public String editTask(@PathVariable Long id, Model model) {
        Optional<ScheduledTask> task = scheduledTaskService.getTask(id);
        if (task.isEmpty()) {
            return "redirect:/web/tasks";
        }
        model.addAttribute("task", task.get());
        return "tasks/form";
    }

    @PostMapping("/tasks")
    public String createTask(@ModelAttribute ScheduledTask task, RedirectAttributes redirectAttributes) {
        try {
            // Set default job class if not provided
            if (task.getJobClass() == null || task.getJobClass().isEmpty()) {
                task.setJobClass("com.eyesdawn.scheduledtask.job.SampleJob");
            }
            
            ScheduledTask createdTask = scheduledTaskService.createTask(task);
            redirectAttributes.addFlashAttribute("message", "Task created successfully: " + createdTask.getName());
            return "redirect:/web/tasks/" + createdTask.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error creating task: " + e.getMessage());
            return "redirect:/web/tasks/new";
        }
    }

    @PostMapping("/tasks/{id}")
    public String updateTask(@PathVariable Long id, @ModelAttribute ScheduledTask task, RedirectAttributes redirectAttributes) {
        try {
            ScheduledTask updatedTask = scheduledTaskService.updateTask(id, task);
            redirectAttributes.addFlashAttribute("message", "Task updated successfully: " + updatedTask.getName());
            return "redirect:/web/tasks/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating task: " + e.getMessage());
            return "redirect:/web/tasks/" + id + "/edit";
        }
    }

    @PostMapping("/tasks/{id}/delete")
    public String deleteTask(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            scheduledTaskService.deleteTask(id);
            redirectAttributes.addFlashAttribute("message", "Task deleted successfully");
            return "redirect:/web/tasks";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting task: " + e.getMessage());
            return "redirect:/web/tasks/" + id;
        }
    }

    @PostMapping("/tasks/{id}/start")
    public String startTask(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            ScheduledTask task = scheduledTaskService.startTask(id);
            redirectAttributes.addFlashAttribute("message", "Task started successfully: " + task.getName());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error starting task: " + e.getMessage());
        }
        return "redirect:/web/tasks/" + id;
    }

    @PostMapping("/tasks/{id}/stop")
    public String stopTask(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            ScheduledTask task = scheduledTaskService.stopTask(id);
            redirectAttributes.addFlashAttribute("message", "Task stopped successfully: " + task.getName());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error stopping task: " + e.getMessage());
        }
        return "redirect:/web/tasks/" + id;
    }

    @PostMapping("/tasks/{id}/pause")
    public String pauseTask(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            ScheduledTask task = scheduledTaskService.pauseTask(id);
            redirectAttributes.addFlashAttribute("message", "Task paused successfully: " + task.getName());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error pausing task: " + e.getMessage());
        }
        return "redirect:/web/tasks/" + id;
    }

    @PostMapping("/tasks/{id}/resume")
    public String resumeTask(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            ScheduledTask task = scheduledTaskService.resumeTask(id);
            redirectAttributes.addFlashAttribute("message", "Task resumed successfully: " + task.getName());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error resuming task: " + e.getMessage());
        }
        return "redirect:/web/tasks/" + id;
    }

    @PostMapping("/tasks/{taskId}/dependencies")
    public String addDependency(@PathVariable Long taskId, @RequestParam Long dependentTaskId, RedirectAttributes redirectAttributes) {
        try {
            taskDependencyService.addDependency(taskId, dependentTaskId);
            redirectAttributes.addFlashAttribute("message", "Dependency added successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error adding dependency: " + e.getMessage());
        }
        return "redirect:/web/tasks/" + taskId;
    }

    @PostMapping("/dependencies/{id}/remove")
    public String removeDependency(@PathVariable Long id, @RequestParam Long taskId, RedirectAttributes redirectAttributes) {
        try {
            taskDependencyService.removeDependency(id);
            redirectAttributes.addFlashAttribute("message", "Dependency removed successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error removing dependency: " + e.getMessage());
        }
        return "redirect:/web/tasks/" + taskId;
    }
}