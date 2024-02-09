# 1. CSV to Database Job

Original idea is from  
https://medium.com/@rostyslav.ivankiv/introduction-to-spring-batch-a2f39454573f

Thıs project creates a Job with two steps

1. Download CSV files from the given URL
2. Load to DB step . Insert the CSV to DB. Each new Job run add CSV with incrementing primary keys

# Run

```
./mvnw spring-boot:run \
-Dspring-boot.run.jvmArguments="-Dspring.batch.job.name=downloadCsvFileJob""
```
