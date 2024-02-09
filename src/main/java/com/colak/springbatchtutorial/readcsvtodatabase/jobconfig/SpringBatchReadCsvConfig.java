package com.colak.springbatchtutorial.readcsvtodatabase.jobconfig;

import com.colak.springbatchtutorial.readcsvtodatabase.jpa.CustomerEntity;
import com.colak.springbatchtutorial.readcsvtodatabase.repository.CustomerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import java.math.BigDecimal;

@Configuration
@Slf4j
public class SpringBatchReadCsvConfig {

    @Bean
    public Job refundJob(JobRepository jobRepository, Step readCsvToDatabaseStep) {
        return new JobBuilder("customers-import", jobRepository)
                .listener(new JobCompletionNotificationListener())
                .start(readCsvToDatabaseStep)
                .build();
    }

    @Bean
    protected Step readCsvToDatabaseStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            FlatFileItemReader<CustomerEntity> customerReader,
            ItemProcessor<CustomerEntity, CustomerEntity> customerProcessor,
            RepositoryItemWriter<CustomerEntity> customerWriter
    ) {
        return new StepBuilder("processCustomerDataStep", jobRepository)
                // The expression chunk(10) specifies that 10 elements are read, processed and then written in each processing step.
                .<CustomerEntity, CustomerEntity>chunk(10, transactionManager)
                .reader(customerReader)
                .processor(customerProcessor)
                .writer(customerWriter)
                //.taskExecutor(taskExecutor())
                .build();
    }

    @Bean
    protected FlatFileItemReader<CustomerEntity> customerReader() {
        FlatFileItemReader<CustomerEntity> itemReader = new FlatFileItemReader<>();
        itemReader.setResource(new FileSystemResource("src/main/resources/customer.csv"));
        itemReader.setName("csv-reader");
        itemReader.setLinesToSkip(1);
        itemReader.setLineMapper(lineMapper());
        return itemReader;
    }

    private LineMapper<CustomerEntity> lineMapper() {
        // Create lineMapper
        DefaultLineMapper<CustomerEntity> lineMapper = new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(getDelimitedLineTokenizer());
        lineMapper.setFieldSetMapper(getFieldSetMapper());

        return lineMapper;
    }

    // Create BeanWrapperFieldSetMapper. This converts a FieldSet object to Java object
    private BeanWrapperFieldSetMapper<CustomerEntity> getFieldSetMapper() {
        BeanWrapperFieldSetMapper<CustomerEntity> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(CustomerEntity.class);
        return fieldSetMapper;
    }


    private DelimitedLineTokenizer getDelimitedLineTokenizer() {
        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(",");
        //  There can be a different number of fields in each line
        lineTokenizer.setStrict(false);
        // The names of the fields
        lineTokenizer.setNames("id", "firstName", "lastName", "balance");
        return lineTokenizer;
    }

    @Bean
    protected ItemProcessor<CustomerEntity, CustomerEntity> customerProcessor(CustomerRepository customerRepository) {
        return customerEntity -> {
            Long customerId = customerEntity.getId();
            BigDecimal refundAmount = customerEntity.getBalance();

            CustomerEntity existingCustomer = customerRepository.findById(customerId).orElse(null);
            if (existingCustomer != null) {
                BigDecimal currentBalance = existingCustomer.getBalance();
                BigDecimal updatedBalance = currentBalance.add(refundAmount);
                existingCustomer.setBalance(updatedBalance);
            } else {
                log.info("User not found customerId: {}", customerId);
            }
            return existingCustomer;
        };
    }

    // Show how to use Spring Data Jpa Repository with Spring Batch
    @Bean
    protected RepositoryItemWriter<CustomerEntity> customerWriter(CustomerRepository customerRepository) {
        RepositoryItemWriter<CustomerEntity> writer = new RepositoryItemWriter<>();
        writer.setRepository(customerRepository);
        writer.setMethodName("save");
        return writer;
    }
}
