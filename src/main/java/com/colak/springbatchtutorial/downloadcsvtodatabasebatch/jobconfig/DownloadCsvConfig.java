package com.colak.springbatchtutorial.downloadcsvtodatabasebatch.jobconfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@RequiredArgsConstructor
@Slf4j
@Configuration
public class DownloadCsvConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Step downloadCsvFileStep(Tasklet downloadCsvFileTasklet) {
        return new StepBuilder("downloadCsvFileStep", jobRepository)
                .tasklet(downloadCsvFileTasklet, transactionManager)
                .build();
    }

    // See https://levelup.gitconnected.com/spring-batch-5-examples-tasklet-and-chunk-processing-ba4860618171
    // The difference between a Tasklet vs. Chunk processing, is that in Tasklet-based processing a Tasklet executes
    // a block of code within the same transaction context, until it returns RepatStatus.FINISHED.
    //
    // In Chunk-based processing we usually process data in chunks, and each chunk executes within its own transaction context,
    // which allows to keep processing even if a transaction encounters an error.
    @Bean
    @StepScope
    protected Tasklet downloadCsvFileTasklet(
            @Value("${sourceFileUrl}") String sourceFileUrl,
            @Value("${targetFilePath}") String targetFilePath
    ) throws MalformedURLException, URISyntaxException {
        return new DownloadCsvFileTasklet(new URI(sourceFileUrl).toURL(), Paths.get(targetFilePath));
    }

    private record DownloadCsvFileTasklet(URL url, Path path) implements Tasklet {

        @Override
        public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
            downloadCsvFile(url, path);
            return RepeatStatus.FINISHED;
        }

        private static void downloadCsvFile(final URL url, final Path path) {
            try (InputStream in = url.openStream()) {
                Files.createDirectories(path.getParent());
                Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
                log.info("File '{}' has been downloaded from '{}'", path, url);
            } catch (IOException e) {
                log.error("Failed to get csv file", e);
            }
        }
    }
}
