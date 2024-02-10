package com.colak.springbatchtutorial.refundbatch.service;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
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
    void testPerformRefundsFromFile() throws JobExecutionException, IOException {
        Path path = getCsvPathFromResource();

        BatchStatus batchStatus = refundService.performRefundsFromFile(path);
        assertFalse(batchStatus.isUnsuccessful());
    }

    // Two examples of getting path to a resource
    private Path getCsvPath() {
        String filePath = "src/test/resources/customer.csv";

        // Create a Path object for the file
        return Paths.get(filePath);
    }

    private Path getCsvPathFromResource() throws IOException {
        String resourcePath = "customer.csv";
        ClassPathResource classPathResource = new ClassPathResource(resourcePath);
        File file = classPathResource.getFile();
        return file.toPath();
    }
}
