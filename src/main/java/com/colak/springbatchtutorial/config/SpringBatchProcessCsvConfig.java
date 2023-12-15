package com.colak.springbatchtutorial.config;

import com.colak.springbatchtutorial.model.PersonCsv;
import com.colak.springbatchtutorial.model.PersonDb;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@RequiredArgsConstructor
@Slf4j
@Configuration
public class SpringBatchProcessCsvConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final DataSource dataSource;


    @Bean
    public Step loadCsvToDatabaseStep(
            ItemReader<PersonCsv> reader,
            ItemProcessor<PersonCsv, PersonDb> processor,
            ItemWriter<PersonDb> writer
    ) {
        return new StepBuilder("loadCsvToDatabaseStep", jobRepository)
                .<PersonCsv, PersonDb>chunk(10, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<PersonCsv> reader(
            @Value("${targetFilePath}") FileSystemResource fileSystemResource
    ) {
        return new FlatFileItemReaderBuilder<PersonCsv>()
                .name("personItemReader")
                .resource(fileSystemResource)
                .linesToSkip(1)
                .delimited()
                .names("person_ID", "name", "first", "last", "middle", "email", "phone", "fax", "title")
                .targetType(PersonCsv.class)
                .build();
    }

    @Bean
    public ItemProcessor<PersonCsv, PersonDb> processor() {
        return personCsv -> {
            if (personCsv.title().contains("Professor")) {
                return null;
            }
            return new PersonDb(personCsv.first(), personCsv.last());
        };
    }

    @Bean
    public JdbcBatchItemWriter<PersonDb> writer() {
        return new JdbcBatchItemWriterBuilder<PersonDb>()
                .sql("INSERT INTO people (first_name, last_name) VALUES (:firstName, :lastName)")
                .dataSource(dataSource)
                .beanMapped()
                .build();
    }

}
