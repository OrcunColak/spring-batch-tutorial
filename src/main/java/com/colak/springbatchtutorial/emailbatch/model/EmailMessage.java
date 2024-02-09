package com.colak.springbatchtutorial.emailbatch.model;

public record EmailMessage(long id,
                           int recipientId,
                           String subject,
                           String content) {
}
