package com.example.project.interfaces;

public interface EmailService {
    void sendPasswordResetCode(String to, String code);
    void emailFallback(String to, String code, Throwable ex);
}
