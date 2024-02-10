package com.colak.springbatchtutorial.refundbatch.mapper;

import com.colak.springbatchtutorial.refundbatch.jpa.CustomerEntity;
import com.colak.springbatchtutorial.refundbatch.dto.CustomerDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    CustomerEntity toCustomer(CustomerDto customerDto);
}
