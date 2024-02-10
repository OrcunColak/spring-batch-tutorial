package com.colak.springbatchtutorial.refundbatch.jobconfig;

import com.colak.springbatchtutorial.refundbatch.dto.CustomerDto;
import com.colak.springbatchtutorial.refundbatch.dto.CustomerRecord;
import com.colak.springbatchtutorial.refundbatch.jpa.CustomerEntity;
import com.colak.springbatchtutorial.refundbatch.mapper.CustomerMapper;
import com.colak.springbatchtutorial.refundbatch.repository.CustomerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import java.math.BigDecimal;

@Configuration
@Slf4j
public class RefundJobConfig {


    // Define a job that reads a csv file and writes to database
    @Bean
    public Job refundJob(JobRepository jobRepository, Step readCsvToDatabaseStep) {
        return new JobBuilder("customers-import", jobRepository)
                .listener(new JobCompletionNotificationListener())
                .start(readCsvToDatabaseStep)
                .build();
    }

    private static class JobCompletionNotificationListener implements JobExecutionListener {
        @Override
        public void afterJob(JobExecution jobExecution) {
            if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
                log.info("JOB FINISHED SUCCESSFULLY!");
            } else {
                log.info("JOB ERROR!");
            }
        }
    }

    @Bean
    protected Step readCsvToDatabaseStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            FlatFileItemReader<CustomerDto> customerReader,
            ItemProcessor<CustomerDto, CustomerEntity> customerProcessor,
            RepositoryItemWriter<CustomerEntity> customerWriter
    ) {
        return new StepBuilder("processCustomerDataStep", jobRepository)
                // The expression chunk(10) specifies that 10 elements are read, processed and then written in each processing step.
                .<CustomerDto, CustomerEntity>chunk(10, transactionManager)
                .reader(customerReader)
                .processor(customerProcessor)
                .writer(customerWriter)
                //.taskExecutor(taskExecutor())
                .build();
    }

    @Bean
    @StepScope
    protected FlatFileItemReader<CustomerDto> customerReader(
            @Value("#{jobParameters['inputFilePath']}") String inputFilePath) {
        if (inputFilePath == null) {
            inputFilePath = "src/main/resources/customer.csv";
        }
        FileSystemResource fileSystemResource = new FileSystemResource(inputFilePath);

        // We can use FlatFileItemReaderBuilder too to create a FlatFileItemReader
        FlatFileItemReader<CustomerDto> itemReader = new FlatFileItemReader<>();
        itemReader.setResource(fileSystemResource);
        itemReader.setName("csv-reader");
        itemReader.setLinesToSkip(1);
        itemReader.setLineMapper(lineMapper());
        return itemReader;
    }

    private LineMapper<CustomerDto> lineMapper() {
        // Create lineMapper
        DefaultLineMapper<CustomerDto> lineMapper = new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(getDelimitedLineTokenizer());
        lineMapper.setFieldSetMapper(getFieldSetMapper());

        // This is just an example that shows how to use FieldSetMapper with a record
        // lineMapper.setFieldSetMapper(new CustomerRecordFieldSetMapper());

        return lineMapper;
    }

    public class CustomerRecordFieldSetMapper implements FieldSetMapper<CustomerRecord> {

        @Override
        public CustomerRecord mapFieldSet(FieldSet fieldSet) {
            return CustomerRecord.builder()
                    .id(fieldSet.readLong("id"))
                    .firstName(fieldSet.readString("firstName"))
                    .lastName(fieldSet.readString("lastName"))
                    .balance(fieldSet.readBigDecimal("balance"))
                    .build();
        }
    }

    // Create BeanWrapperFieldSetMapper. This converts a FieldSet object to Java object
    private BeanWrapperFieldSetMapper<CustomerDto> getFieldSetMapper() {
        BeanWrapperFieldSetMapper<CustomerDto> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(CustomerDto.class);
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
    protected ItemProcessor<CustomerDto, CustomerEntity> customerProcessor(CustomerRepository customerRepository,
                                                                           CustomerMapper customerMapper) {
        return customerDto -> {
            CustomerEntity customerEntity = customerMapper.toCustomer(customerDto);

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
            return customerEntity;

        };
    }

    // Show how to use Spring Data Jpa Repository with Spring Batch
    @Bean
    protected RepositoryItemWriter<CustomerEntity> customerWriter(CustomerRepository customerRepository) {
        RepositoryItemWriter<CustomerEntity> writer = new RepositoryItemWriter<>();
        writer.setRepository(customerRepository);
        // This call is not necessary
        // writer.setMethodName("save");
        return writer;
    }
}
