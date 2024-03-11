package com.colak.springbatchtutorial.flatfilebatch.service;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FlatFileService {

    private final JobLauncher jobLauncher;
    private final Job readFlatFileJob;

    public BatchStatus performReadFlatFile() throws JobExecutionException {
        JobParameters jobParameters = new JobParameters();
        JobExecution jobExecution = jobLauncher.run(readFlatFileJob, jobParameters);
        return jobExecution.getStatus();
    }
}
