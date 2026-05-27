package com.example.project.services;

import com.example.project.entity.User;
import com.example.project.exceptions.InvalidTokenException;
import com.example.project.exceptions.UserNotFoundByEmailException;
import com.example.project.interfaces.EmailService;
import com.example.project.interfaces.ResetPassword;
import com.example.project.interfaces.SendResetToken;
import com.example.project.metrics.AuthMetricsService;
import com.example.project.metrics.PerformanceMetricsService;
import com.example.project.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Slf4j
@Service
@AllArgsConstructor
public class PasswordResetServiceImpl implements SendResetToken, ResetPassword {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthMetricsService metrics;
    private final PerformanceMetricsService performanceMetrics;
    private final EmailService emailService;

    private static final Random random = new Random();
    private static final long TOKEN_LIFETIME_MINUTES = 1;

    @Override
    public String sendResetToken(String email) {
        return performanceMetrics.sendResetTokenTimer().record(() -> {
            log.info("Запрос сброса пароля для: {}", email);

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UserNotFoundByEmailException("User not found"));

            int code = 100000 + random.nextInt(900000);
            String token = String.valueOf(code);

            user.setResetToken(token);
            user.setResetTokenCreatedAt(LocalDateTime.now());
            userRepository.save(user);

            emailService.sendPasswordResetCode(email, token);

            metrics.incrementResetTokenSent();
            return token;
        });
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        performanceMetrics.resetPasswordTimer().record(() -> {
            log.info("Сброс пароля по токену");

            User user = userRepository.findByResetToken(token)
                    .orElseThrow(() -> new InvalidTokenException("Invalid token"));

            if (user.getResetTokenCreatedAt()
                    .plusMinutes(TOKEN_LIFETIME_MINUTES)
                    .isBefore(LocalDateTime.now())) {
                throw new InvalidTokenException("Token expired");
            }

            user.setPassword(passwordEncoder.encode(newPassword));
            user.setResetToken(null);
            user.setResetTokenCreatedAt(null);
            userRepository.save(user);

            metrics.incrementPasswordReset();
            log.info("Пароль успешно сброшен");
        });
    }
}