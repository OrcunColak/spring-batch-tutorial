package com.colak.springbatchtutorial.readcsvtodatabase.service;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RefundServiceTest {
    @Autowired
    RefundService refundService;


    @Test
    void testPerformRefunds() throws JobExecutionException {
        refundService.performRefunds();
    }
}
