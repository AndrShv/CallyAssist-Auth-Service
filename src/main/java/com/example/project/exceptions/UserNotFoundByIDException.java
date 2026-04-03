package com.example.project.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNotFoundByIDException extends RuntimeException{
    public UserNotFoundByIDException(String message) {
        super(message);
    }
}
