package com.eyesdawn.scheduledtask;

import com.eyesdawn.scheduledtask.model.ScheduledTask;
import com.eyesdawn.scheduledtask.service.ScheduledTaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ScheduledTaskApplicationTests {

    @Autowired
    private ScheduledTaskService scheduledTaskService;

    @Test
    void contextLoads() {
        assertThat(scheduledTaskService).isNotNull();
    }

    @Test
    void testCreateTask() {
        ScheduledTask task = new ScheduledTask();
        task.setName("test-task");
        task.setDescription("Test task description");
        task.setCronExpression("0/30 * * * * ?");
        task.setJobClass("com.eyesdawn.scheduledtask.job.SampleJob");
        task.setMaxRetries(3);
        task.setTimeoutSeconds(300L);

        ScheduledTask savedTask = scheduledTaskService.createTask(task);
        
        assertThat(savedTask).isNotNull();
        assertThat(savedTask.getId()).isNotNull();
        assertThat(savedTask.getName()).isEqualTo("test-task");
        assertThat(savedTask.getStatus()).isEqualTo(ScheduledTask.TaskStatus.INACTIVE);
    }
}