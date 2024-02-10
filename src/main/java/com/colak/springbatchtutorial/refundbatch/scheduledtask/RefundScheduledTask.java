package com.colak.springbatchtutorial.refundbatch.scheduledtask;

import com.colak.springbatchtutorial.refundbatch.service.RefundService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.TimeUnit;

@Component
@EnableScheduling

@RequiredArgsConstructor
@Slf4j
public class RefundScheduledTask {

    private final RefundService refundService;

    // Run every day of the week at 00:00.
    //@Scheduled(cron = "0 0 0 * * ?")
    @Scheduled(fixedRate = 2, timeUnit = TimeUnit.SECONDS)
    public void performScheduledRefunds() {
        Path watchPath = Paths.get("D:/Work");

        try {
            WatchService watchService = FileSystems.getDefault().newWatchService();
            watchPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

            WatchKey key;
            // This method will block until events occur.
            while ((key = watchService.take()) != null) {
                for (WatchEvent<?> event : key.pollEvents()) {

                    log.info("Event kind: {}. File affected:{}.", event.kind(), event.context());
                    if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                        Path filePath = watchPath.resolve((Path) event.context());
                        refundService.performRefundsFromFile(filePath);
                    }
                }
                // If you forget to reset the key, it won't receive further events.
                key.reset();
            }
        } catch (IOException | JobExecutionException exception) {
            log.error(exception.getMessage(), exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }
}
