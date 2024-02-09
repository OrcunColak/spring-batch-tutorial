package com.colak.springbatchtutorial.readcsvtodatabase.scheduledtask;

import com.colak.springbatchtutorial.readcsvtodatabase.service.RefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RefundScheduledTask {

    private final RefundService refundService;

    // Run every day of the week at 00:00.
    @Scheduled(cron = "0 0 0 * * ?")
    public void performScheduledRefunds() throws JobExecutionException {
        refundService.performRefunds();
    }
}
