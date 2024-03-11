package com.colak.springbatchtutorial.flatfilebatch.jobconfig;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@Slf4j
public class FlatFileJobConfig {

    @Bean
    public Job readFlatFileJob(JobRepository jobRepository, Step readFileStep) {
        return new JobBuilder("read-flat-file", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(readFileStep)
                .build();
    }

    @Bean
    protected Step readFileStep(JobRepository jobRepository,
                                PlatformTransactionManager transactionManager,
                                ItemReader<String> flatFileItemReader,
                                ItemProcessor<String, String> flatFileItemProcessor,
                                ItemWriter<String> flatFileItemWriter) {
        return new StepBuilder("processFileStep", jobRepository)
                // The expression chunk(10) specifies that 10 elements are read, processed and then written in each processing step.
                .<String, String>chunk(10, transactionManager)
                .reader(flatFileItemReader)
                .processor(flatFileItemProcessor)
                .writer(flatFileItemWriter)
                .build();
    }

    @Bean
    protected ItemReader<String> flatFileItemReader() {
        return new FlatFileItemReaderBuilder<String>()
                .name("itemReader")
                .resource(new ClassPathResource("customer.csv"))
                .lineTokenizer(lineTokenizer())
                .lineMapper(createLineMapper())
                .build();
    }

    private LineTokenizer lineTokenizer() {
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setDelimiter(System.lineSeparator()); // Assuming each line is terminated by a newline character
        tokenizer.setStrict(false); // false to handle lines with varying lengths
        return tokenizer;
    }

    private LineMapper<String> createLineMapper() {
        return (line, lineNumber) -> line;
    }

    @Bean
    protected ItemProcessor<String, String> flatFileItemProcessor() {
        return String::toUpperCase;
    }

    @Bean
    protected ItemWriter<String> flatFileItemWriter() {
        return items -> {
            for (String item : items) {
                log.info("Writing item: " + item);
            }
        };
    }
}
