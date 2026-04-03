package com.example.project.services;

import com.example.project.dto.VoiceRequestDTO;
import com.example.project.dto.VoiceResponseDTO;
import com.example.project.entity.User;
import com.example.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoiceService {

    private static final int FREE_LIMIT = 10;

    private final UserRepository userRepository;

    @Transactional
    public VoiceResponseDTO process(UUID userId, VoiceRequestDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        if (!user.canUseVoice()) {
            log.warn("Voice limit exceeded: userId={}, today={}", userId, user.getVoiceRequestsToday());
            throw new VoiceLimitExceededException(
                    "Дневной лимит %d голосовых запросов исчерпан. Обнови подписку.".formatted(FREE_LIMIT)
            );
        }

        user.incrementVoiceRequests();
        userRepository.save(user);

        log.info("Voice request: userId={}, today={}/{}, plan={}",
                userId, user.getVoiceRequestsToday(), FREE_LIMIT, user.getSubscriptionPlan());

        // TODO: передай dto.getText() в LLM / STT pipeline
        return VoiceResponseDTO.builder()
                .response("Обрабатываю: \"" + dto.getText() + "\"")
                .voiceRequestsToday(user.getVoiceRequestsToday())
                .voiceRequestsLimit(FREE_LIMIT)
                .remainingToday(Math.max(0, FREE_LIMIT - user.getVoiceRequestsToday()))
                .isPaidUser(user.hasActivePaidSubscription())
                .build();
    }

    // ── исключение ────────────────────────────────────────────────

    public static class VoiceLimitExceededException extends RuntimeException {
        public VoiceLimitExceededException(String message) { super(message); }
    }
}
