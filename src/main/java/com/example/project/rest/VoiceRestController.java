package com.example.project.rest;

import com.example.project.dto.VoiceRequestDTO;
import com.example.project.dto.VoiceResponseDTO;
import com.example.project.services.VoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/voice")
@RequiredArgsConstructor
public class VoiceRestController {

    private final VoiceService voiceService;

    // POST /api/voice/request
    // Headers: Authorization: Bearer <token>
    // Body: { "text": "Создай встречу с Андреем завтра в 14:00" }
    //
    // FREE: до 10 запросов в день → потом 429
    // PRO / ENTERPRISE / ADMIN: безлимитно
    @PostMapping("/request")
    public ResponseEntity<VoiceResponseDTO> request(
            @Valid @RequestBody VoiceRequestDTO dto,
            Authentication authentication
    ) {
        UUID userId = UUID.fromString(authentication.getPrincipal().toString());
        return ResponseEntity.ok(voiceService.process(userId, dto));
    }
}
