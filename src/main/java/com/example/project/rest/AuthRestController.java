package com.example.project.rest;

import com.example.project.dto.*;
import com.example.project.interfaces.*;
import com.example.project.services.custom.CustomOidcUser;
import com.example.project.services.custom.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    private final GetSubscriptionInfo getSubscriptionInfo;

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

        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }

        Object principal = authentication.getPrincipal();

        UUID userId;

        if (principal instanceof CustomUserDetails cud) {
            userId = cud.getId();
        } else if (principal instanceof CustomOidcUser oidcUser) {
            userId = oidcUser.getUser().getId();
        } else {
            userId = UUID.fromString(principal.toString());
        }

        return ResponseEntity.ok(getMe.getMe(userId));
    }

    @GetMapping("/subscription")
    public ResponseEntity<SubscriptionInfoDTO> getSubscription(
            Authentication authentication,
            @RequestParam(required = false) UUID userId
    ) {
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof CustomUserDetails user) {
                return ResponseEntity.ok(getSubscriptionInfo.getSubscriptionInfo(user.getId()));
            }
        }
        if (userId != null) {
            return ResponseEntity.ok(getSubscriptionInfo.getSubscriptionInfo(userId));
        }
        return ResponseEntity.status(401).build();
    }
    
}