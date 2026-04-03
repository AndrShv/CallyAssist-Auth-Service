package com.example.project.services;

import com.example.project.dto.UserLoginDTO;
import com.example.project.dto.UserResponseDTO;
import com.example.project.entity.User;
import com.example.project.mappers.UserMapper;
import com.example.project.services.custom.CustomUserDetails;
import com.example.project.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;

    public UserResponseDTO login(UserLoginDTO dto) {
        String email = dto.getEmail().trim().toLowerCase(Locale.ROOT);
        log.info("Login attempt: {}", email);

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, dto.getPassword())
        );

        CustomUserDetails details = (CustomUserDetails) auth.getPrincipal();
        User user = details.getUser();

        String token = jwtUtil.generateToken(user.getEmail(), user.getId(), List.of(user.getRole()));
        log.info("Login success: {} ({})", user.getEmail(), user.getId());

        return userMapper.toUserResponseDTO(user, token);
    }
}
