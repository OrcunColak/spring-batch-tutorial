package com.colak.springbatchtutorial.refundbatch.service;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class RefundServiceTest {
    @Autowired
    RefundService refundService;

    @Test
    void testPerformRefunds() throws JobExecutionException {
        BatchStatus batchStatus = refundService.performRefunds();
        assertTrue(batchStatus.isRunning());
    }

    @Test
    void testPerformRefundsFromFile() throws JobExecutionException {
        String filePath = "src/main/resources/customer.csv";

        // Create a Path object for the file
        Path path = Paths.get(filePath);

        BatchStatus batchStatus = refundService.performRefundsFromFile(path);
        assertFalse(batchStatus.isUnsuccessful());
    }
}
