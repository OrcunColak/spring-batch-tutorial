package com.colak.springbatchtutorial.flatfilebatch.service;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class FlatFileServiceTest {

    @Autowired
    private FlatFileService flatFileService;


    @Test
    void testPerformReadFlatFile() throws JobExecutionException {
        BatchStatus batchStatus = flatFileService.performReadFlatFile();
        //assertFalse(batchStatus.isUnsuccessful());
    }
}
