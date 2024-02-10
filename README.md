# 1. Download CSV and Write to Database Job

Original idea is from  
https://medium.com/@rostyslav.ivankiv/introduction-to-spring-batch-a2f39454573f

# 2. RefundBatch - Read CSV and Write to Database Job

The original idea is from
https://medium.com/@berkinyardimci98/data-processing-with-spring-batch-5-b10de6f23262

# 3. EmailBatch - Read DB and Write To File Job

The original idea is from
https://levelup.gitconnected.com/spring-batch-5-examples-tasklet-and-chunk-processing-ba4860618171

# Passing Job Parameter Example

The original idea is from
https://satyacodes.medium.com/spring-boot-batch-guide-example-with-csv-to-database-fe6e29d364cd

# spring.batch.job.name

We can start a job from command line using spring.batch.job.name parameter

```
./mvnw spring-boot:run \
-Dspring-boot.run.jvmArguments="-Dspring.batch.job.name=downloadCsvFileJob""
```
