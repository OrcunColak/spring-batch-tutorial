package com.colak.springbatchtutorial.downloadcsvtodatabasebatch.model;

public record PersonCsv(
        String person_ID,
        String name,
        String first,
        String last,
        String middle,
        String email,
        String phone,
        String fax,
        String title
) {
}