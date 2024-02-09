package com.colak.springbatchtutorial.emailbatch.jobconfig;

import com.colak.springbatchtutorial.emailbatch.model.EmailMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.annotation.OnReadError;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.skip.SkipLimitExceededException;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.repeat.CompletionPolicy;
import org.springframework.batch.repeat.policy.CompositeCompletionPolicy;
import org.springframework.batch.repeat.policy.SimpleCompletionPolicy;
import org.springframework.batch.repeat.policy.TimeoutTerminationPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;

@Configuration
@Slf4j
public class EmailJobConfig {

    // Define a job that reads emails from a table and writes to a file
    @Bean
    public Job readEmailjob(JobRepository jobRepository, Step readDatabaseToFileStep) {
        return new JobBuilder("email-export", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(readDatabaseToFileStep)
                .build();
    }

    @Bean
    public Step readDatabaseToFileStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            JdbcCursorItemReader<EmailMessage> emailMessageJdbcCursorItemReader,
            FlatFileItemWriter<EmailMessage> emailMessageFlatFileItemWriter) {

        return new StepBuilder("processEmailsStep", jobRepository)
                .<EmailMessage, EmailMessage>chunk(completionPolicy(), transactionManager)
                .reader(emailMessageJdbcCursorItemReader)
                .writer(emailMessageFlatFileItemWriter)
                .faultTolerant()
                .skipPolicy(new DbRowVerificationSkipper())
                .listener(new DbReaderListener())
                .build();
    }

    // The completionPolicy sets the timeout in milliseconds before gracefully exiting if the latest result does not come in,
    // and sets the chunk size to 10, which would be the number of items to read in a single chunk. In our case an item would be a table row from the database.
    private CompletionPolicy completionPolicy() {
        CompositeCompletionPolicy policy = new CompositeCompletionPolicy();
        policy.setPolicies(
                new CompletionPolicy[]{
                        new TimeoutTerminationPolicy(1000),
                        new SimpleCompletionPolicy(10)});
        return policy;
    }

    private static class DbRowVerificationSkipper implements SkipPolicy {
        @Override
        public boolean shouldSkip(Throwable t, long skipCount) throws SkipLimitExceededException {
            // this is just an example, find a proper skip policy
            if (t instanceof SQLException) {
                return false;
            } else return skipCount <= 10;
        }
    }

    private static class DbReaderListener {
        @OnReadError
        public void onReadError(Exception e) {
            log.error(e.getMessage());
        }

    }

    @Bean
    public JdbcCursorItemReader<EmailMessage> emailMessageJdbcCursorItemReader(DataSource dataSource) {
        return new JdbcCursorItemReaderBuilder<EmailMessage>()
                .name("messageItemReader")
                .dataSource(dataSource)
                .sql("SELECT * FROM my_schema.message")
                .rowMapper(new MessageRowMapper())
                .build();
    }

    protected static class MessageRowMapper implements RowMapper<EmailMessage> {
        @Override
        public EmailMessage mapRow(ResultSet resultSet, int rowNumber) throws SQLException {
            return new EmailMessage(
                    resultSet.getLong("message_id"),
                    resultSet.getInt("recipient_id"),
                    resultSet.getString("subject"),
                    resultSet.getString("content")
            );
        }
    }

    @Bean
    public Resource myFileResource() {
        return new FileSystemResource("./messages_from_db");
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<EmailMessage> emailMessageFlatFileItemWriter(Resource myFileResource) {
        return new FlatFileItemWriterBuilder<EmailMessage>()
                .name("myFileWriter")
                .resource((WritableResource) myFileResource)
                .formatted()
                .format("%s;%s;%s;%s;%s")
                .names(new String[]{
                        "id",
                        "recipientId",
                        "subject",
                        "content",
                        "status",
                })
                .build();
    }
}
