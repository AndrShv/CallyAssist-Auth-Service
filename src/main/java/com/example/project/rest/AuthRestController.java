package com.example.project.rest;

import com.example.project.dto.UserLoginDTO;
import com.example.project.dto.UserRegisterDTO;
import com.example.project.dto.UserResponseDTO;
import com.example.project.entity.User;
import com.example.project.interfaces.GetMe;
import com.example.project.interfaces.Register;

import com.example.project.repository.UserRepository;
import com.example.project.services.LoginService;
import com.example.project.util.JwtUtil;
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

    private final Register register;
    private final LoginService loginService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody UserRegisterDTO dto) {
        log.debug("REST request to register User : {}", dto.getEmail());
        return ResponseEntity.ok(register.register(dto));
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponseDTO> login(@Valid @RequestBody UserLoginDTO dto) {
        log.debug("REST request to login User : {}", dto.getEmail());
        return ResponseEntity.ok(loginService.login(dto));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        String userId = (String) authentication.getPrincipal();
        User user = userRepository.findById(UUID.fromString(userId)).orElse(null);

        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        UserResponseDTO response = UserResponseDTO.builder()
                .id(String.valueOf(user.getId()))
                .username(user.getUsername())
                .email(user.getEmail())
                .role(String.valueOf(user.getRole()))
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/oauth2/token")
    public ResponseEntity<Map<String, String>> oauth2Token(@RequestParam String token) {
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.badRequest().body(Map.of("error", "invalid token"));
        }

        return ResponseEntity.ok(Map.of(
                "token", token,
                "type", "Bearer",
                "email", jwtUtil.getEmailFromToken(token)
        ));
    }
}