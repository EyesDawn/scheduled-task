package com.eyesdawn.scheduledtask.job;

import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SampleJob extends BaseScheduledJob {

    private static final Logger logger = LoggerFactory.getLogger(SampleJob.class);

    @Override
    protected String executeJob(JobExecutionContext context) throws Exception {
        String jobData = context.getJobDetail().getJobDataMap().getString("jobData");
        logger.info("Executing sample job with data: {}", jobData);
        
        // Simulate some work
        Thread.sleep(2000);
        
        if (isInterrupted()) {
            logger.info("Sample job was interrupted");
            return "Job was interrupted";
        }
        
        String result = "Sample job completed successfully at " + java.time.LocalDateTime.now();
        logger.info("Sample job result: {}", result);
        
        return result;
    }
}