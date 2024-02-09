# 1. Download CSV Write to Database Job

Original idea is from  
https://medium.com/@rostyslav.ivankiv/introduction-to-spring-batch-a2f39454573f

# 2. Read CSV Write to Database Job

The original idea is from
https://medium.com/@berkinyardimci98/data-processing-with-spring-batch-5-b10de6f23262

# spring.batch.job.name

We can start a job from command line using spring.batch.job.name parameter

```
./mvnw spring-boot:run \
-Dspring-boot.run.jvmArguments="-Dspring.batch.job.name=downloadCsvFileJob""
```
