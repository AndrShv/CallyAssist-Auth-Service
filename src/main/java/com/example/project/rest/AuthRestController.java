package com.example.project.rest;

import com.example.project.dto.AuthInfoDTO;
import com.example.project.dto.UserLoginDTO;
import com.example.project.dto.UserRegisterDTO;
import com.example.project.dto.UserResponseDTO;
import com.example.project.interfaces.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthRestController {

    private final Register  register;
    private final Login  login;
    private final GetMe getMe;
    private final GenerateTokenForOAuth2 generateTokenForOAuth2;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody UserRegisterDTO dto) {
        log.debug("REST request to register user: {}", dto.getEmail());
        return ResponseEntity.ok(register.register(dto));
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponseDTO> login(@Valid @RequestBody UserLoginDTO dto) {
        log.debug("REST request to login user: {}", dto.getEmail());
        return ResponseEntity.ok(login.login(dto));
    }

    @GetMapping("/me")
    public ResponseEntity<AuthInfoDTO> me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            log.error("Unauthorized access to /me endpoint");
            return ResponseEntity.status(401).build();
        }

        log.debug("REST request to get user: {}", authentication.getName());
        UUID userId = UUID.fromString(authentication.getPrincipal().toString());
        return ResponseEntity.ok(getMe.getMe((userId)));
    }

    @GetMapping("/oauth2/token")
    public ResponseEntity<Map<String, String>> oauth2Token(@RequestParam UUID userId) {
        log.debug("REST request to generate OAuth2 token for userId: {}", userId);

        String token = generateTokenForOAuth2.generateTokenForOAuth2(userId);

        return ResponseEntity.ok(Map.of(
                "token", token,
                "type", "Bearer"
        ));
    }
}