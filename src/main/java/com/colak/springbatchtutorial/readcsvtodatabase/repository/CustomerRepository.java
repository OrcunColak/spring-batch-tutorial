package com.colak.springbatchtutorial.readcsvtodatabase.repository;

import com.colak.springbatchtutorial.readcsvtodatabase.jpa.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<CustomerEntity,Long> {
}
