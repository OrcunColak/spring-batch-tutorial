package com.colak.springbatchtutorial.refundbatch.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record CustomerRecord(Long id,
                             String firstName,
                             String lastName,
                             BigDecimal balance) {
}
