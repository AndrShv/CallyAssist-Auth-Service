package com.example.project.services;


import com.example.project.exceptions.EmailSendingExeption;
import com.example.project.metrics.EmailMetricsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private EmailMetricsService emailMetrics;

    @InjectMocks
    private EmailServiceImpl emailService;

    // ============================
    // sendPasswordResetCode
    // ============================

    @Test
    void sendPasswordResetCode_success() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        emailService.sendPasswordResetCode("andrey@test.com", "123456");

        // проверяем что письмо ушло с правильным получателем
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage sent = captor.getValue();
        assertArrayEquals(new String[]{"andrey@test.com"}, sent.getTo());
        assertEquals("Password reset", sent.getSubject());
        assertTrue(sent.getText().contains("123456"));

        verify(emailMetrics).sent();
        verify(emailMetrics, never()).failed();
    }

    @Test
    void sendPasswordResetCode_smtpError_shouldIncrementFailedAndThrow() {
        doThrow(new RuntimeException("SMTP connection refused"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        assertThrows(EmailSendingExeption.class,
                () -> emailService.sendPasswordResetCode("andrey@test.com", "123456"));

        verify(emailMetrics).failed();
        verify(emailMetrics, never()).sent();
    }

    @Test
    void sendPasswordResetCode_messageContainsCode() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        emailService.sendPasswordResetCode("user@test.com", "654321");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        String text = captor.getValue().getText();
        assertNotNull(text);
        assertTrue(text.contains("654321"), "Письмо должно содержать код 654321");
    }

    @Test
    void sendPasswordResetCode_differentRecipients_sendToCorrectEmail() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        emailService.sendPasswordResetCode("first@test.com", "111111");
        emailService.sendPasswordResetCode("second@test.com", "222222");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(2)).send(captor.capture());

        assertArrayEquals(new String[]{"first@test.com"}, captor.getAllValues().get(0).getTo());
        assertArrayEquals(new String[]{"second@test.com"}, captor.getAllValues().get(1).getTo());
        verify(emailMetrics, times(2)).sent();
    }

    @Test
    void sendPasswordResetCode_smtpError_doesNotIncrementSent() {
        doThrow(new RuntimeException("Timeout"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        assertThrows(EmailSendingExeption.class,
                () -> emailService.sendPasswordResetCode("user@test.com", "000000"));

        verify(emailMetrics, never()).sent();
        verify(emailMetrics).failed();
    }

    @Test
    void sendPasswordResetCode_wrapsOriginalCause() {
        RuntimeException cause = new RuntimeException("SMTP error");
        doThrow(cause).when(mailSender).send(any(SimpleMailMessage.class));

        EmailSendingExeption ex = assertThrows(EmailSendingExeption.class,
                () -> emailService.sendPasswordResetCode("user@test.com", "123456"));

        assertEquals(cause, ex.getCause());
    }
}
