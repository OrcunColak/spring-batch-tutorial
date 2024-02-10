package com.colak.springbatchtutorial.refundbatch.controller;

import com.colak.springbatchtutorial.refundbatch.service.RefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping(path = "/api/refund")
@RequiredArgsConstructor
public class RefundController {

    private final RefundService refundService;

    @PostMapping("/upload")
    public BatchStatus uploadFile(@RequestBody MultipartFile file) throws IOException, JobExecutionException {
        // Save the uploaded file to a temporary location
        Path tempFile = Files.createTempFile("temp_users", ".csv");
        file.transferTo(tempFile.toFile());

        BatchStatus batchStatus = refundService.performRefundsFromFile(tempFile);

        Files.delete(tempFile);

        return batchStatus;
    }
}
