package com.colak.springbatchtutorial.refundbatch.repository;

import com.colak.springbatchtutorial.refundbatch.jpa.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<CustomerEntity,Long> {
}
