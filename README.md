# Read Me First
Original idea is from  
https://medium.com/@rostyslav.ivankiv/introduction-to-spring-batch-a2f39454573f

Thıs project creates a Job with two steps
1. Download step
2. Load to DB step

# Download step
Download CSV files from the given URL

# Load to DB step
Insert the CSV to DB. Each new Job run add CSV with incrementing primary keys
