package com.example.project.exceptions;

public class EmailSendingExeption extends  RuntimeException {
    public EmailSendingExeption(String message, Throwable cause) {
        super(message, cause);
    }
}
