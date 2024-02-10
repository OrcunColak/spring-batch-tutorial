package com.colak.springbatchtutorial.refundbatch.integrationtest;

import com.colak.springbatchtutorial.refundbatch.jobconfig.RefundJobConfig;
import com.colak.springbatchtutorial.refundbatch.repository.CustomerRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBatchTest
@SpringJUnitConfig(classes = {RefundJobConfig.class, RefundTestConfig.class})
class RefundIntegrationTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;

    @Autowired
    private Job refundJob;

    @AfterEach
    public void cleanUp() {
        jobRepositoryTestUtils.removeJobExecutions();
    }

    @Test
    void testPerformRefundsFromFile(@Autowired CustomerRepository customerRepository) throws Exception {
        JobParameters jobParameters = defaultJobParameters();

        jobLauncherTestUtils.setJob(refundJob);
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
        JobInstance actualJobInstance = jobExecution.getJobInstance();
        ExitStatus actualJobExitStatus = jobExecution.getExitStatus();

        assertThat(actualJobInstance.getJobName()).isEqualTo("customers-import");
        assertThat(actualJobExitStatus.getExitCode()).isEqualTo(ExitStatus.COMPLETED.getExitCode());
        assertThat(customerRepository.findAll()).hasSize(2);
    }

    private JobParameters defaultJobParameters() throws IOException {
        JobParametersBuilder paramsBuilder = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .addString("inputFilePath", getCsvPathFromResource());


        return paramsBuilder.toJobParameters();
    }

    private String getCsvPathFromResource() throws IOException {
        String resourcePath = "customer.csv";
        ClassPathResource classPathResource = new ClassPathResource(resourcePath);
        File file = classPathResource.getFile();
        return file.toPath().toString();
    }
}
