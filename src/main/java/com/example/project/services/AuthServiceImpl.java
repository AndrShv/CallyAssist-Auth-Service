package com.example.project.services;

import com.example.project.dto.*;
import com.example.project.entity.User;
import com.example.project.enums.Role;
import com.example.project.enums.SubscriptionPlan;
import com.example.project.exceptions.UserAlreadyExistsException;
import com.example.project.exceptions.UserNotFoundByIDException;
import com.example.project.interfaces.*;
import com.example.project.mappers.UserMapper;
import com.example.project.repository.UserRepository;
import com.example.project.services.custom.CustomUserDetails;
import com.example.project.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements Register, Login, GetMe, GenerateTokenForOAuth2, GetSubscriptionInfo {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public UserResponseDTO register(UserRegisterDTO dto) {
        String email = dto.getEmail().trim().toLowerCase(Locale.ROOT);
        log.info("Registering user: {}", email);

        if (userRepository.existsByEmailIgnoreCase(email)) {
            log.error("Registration failed: user with email '{}' already exists", email);
            throw new UserAlreadyExistsException("Пользователь уже существует");
        }

        Role role;
        try {
            role = dto.getRole() != null
                    ? Role.valueOf(dto.getRole().toUpperCase(Locale.ROOT))
                    : Role.USER;
        } catch (IllegalArgumentException e) {
            log.warn("Unknown role '{}', defaulting to USER", dto.getRole());
            role = Role.USER;
        }

        User user = User.builder()
                .email(email)
                .username(dto.getUsername().trim())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(role)
                .subscriptionPlan(SubscriptionPlan.FREE)
                .active(true)
                .voiceRequestsToday(0)
                .build();

        user = userRepository.save(user);
        log.info("Registered: {} ({})", user.getEmail(), user.getId());
        return userMapper.toUserResponseDTO(user, null);
    }

    @Override
    public UserResponseDTO login(UserLoginDTO dto) {
        String email = dto.getEmail().trim().toLowerCase(Locale.ROOT);
        log.info("Login attempt: {}", email);

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + email));

        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new RuntimeException("Пользователь зарегистрирован через OAuth2, войдите через Google");
        }

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, dto.getPassword())
        );

        CustomUserDetails details = (CustomUserDetails) auth.getPrincipal();
        user = details.getUser();

        String token = jwtUtil.generateToken(user.getEmail(), user.getId(), List.of(user.getRole()));
        log.info("Login success: {} ({})", user.getEmail(), user.getId());

        return userMapper.toUserResponseDTO(user, token);
    }
    @Override
    @Transactional(readOnly = true)
    public String generateTokenForOAuth2(UUID userId) {
        log.info("Generating token for userId: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundByIDException("User not found: " + userId));

        return jwtUtil.generateToken(user.getEmail(), user.getId(), List.of(user.getRole()));
    }

    @Override
    @Transactional(readOnly = true)
    public AuthInfoDTO getMe(UUID userId) {
        log.info("GetMe for userId: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundByIDException("User not found: " + userId));

        return userMapper.toAuthInfoDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionInfoDTO getSubscriptionInfo(UUID userId) {
        log.info("Getting subscription info for userId: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundByIDException("User not found: " + userId));

        return SubscriptionInfoDTO.builder()
                .subscriptionPlan(user.getSubscriptionPlan())
                .subscriptionExpiresAt(user.getSubscriptionExpiresAt())
                .active(user.getActive())
                .build();
    }
}