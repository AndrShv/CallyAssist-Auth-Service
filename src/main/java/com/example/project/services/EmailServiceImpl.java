package com.example.project.services;

import com.example.project.exceptions.EmailSendingExeption;
import com.example.project.interfaces.EmailService;
import com.example.project.metrics.EmailMetricsService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final EmailMetricsService emailMetrics;

    @Override
    @CircuitBreaker(name = "emailService", fallbackMethod = "emailFallback")
    @Retry(name = "emailService")
    public void sendPasswordResetCode(String to, String code) {
        log.info("Отправка письма сброса пароля на: {}", to);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Password reset");
        message.setText("Ваш код: " + code);

        try {
            mailSender.send(message);
            emailMetrics.sent();
            log.info("Письмо успешно отправлено на: {}", to);
        } catch (Exception ex) {
            emailMetrics.failed();
            log.error("Ошибка отправки письма на {}: {}", to, ex.getMessage());
            throw new EmailSendingExeption("Email service unavailable", ex);
        }
    }
    @Override
    public void emailFallback(String to, String code, Throwable ex) {

        emailMetrics.failed();

        log.error("Email fallback activated for {} : {}", to, ex.getMessage());

        throw new EmailSendingExeption("Email service unavailable", ex);
    }
}