package com.colak.springbatchtutorial.refundbatch.service;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RefundService {

    private final JobLauncher jobLauncher;
    private final Job refundJob;

    public BatchStatus performRefunds() throws JobExecutionException {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();
        JobExecution jobExecution = jobLauncher.run(refundJob, jobParameters);
        return jobExecution.getStatus();
    }

    public BatchStatus performRefundsFromFile(Path filePath) throws JobExecutionException {
        // Set the file path in the job parameters
        Map<String, JobParameter<?>> maps = new HashMap<>();
        maps.put("timestamp", new JobParameter<>(System.currentTimeMillis(), Long.class));
        maps.put("inputFilePath", new JobParameter<>(filePath.toAbsolutePath().toString(), String.class));

        JobParameters parameters = new JobParameters(maps);
        // Run the job with the updated parameters
        JobExecution jobExecution = jobLauncher.run(refundJob, parameters);

        return jobExecution.getStatus();
    }
}
