package com.colak.springbatchtutorial.downloadcsvtodatabase.jobconfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Slf4j
@Configuration
public class DownloadCSVToDatabaseJobConfig {

    private final JobRepository jobRepository;

    @Bean
    public Job downloadCsvFileJob(Step downloadCsvFileStep, Step loadCsvToDatabaseStep) {
        return new JobBuilder("downloadCsvFileJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(downloadCsvFileStep)
                .next(loadCsvToDatabaseStep)
                .build();
    }

}
