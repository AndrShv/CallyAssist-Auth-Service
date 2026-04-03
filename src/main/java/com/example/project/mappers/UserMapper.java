package com.example.project.mappers;

import com.example.project.dto.AuthInfoDTO;
import com.example.project.dto.UserResponseDTO;
import com.example.project.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponseDTO toUserResponseDTO(User user, String token) {
        return UserResponseDTO.builder()
                .id(user.getId().toString())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .token(token)
                .build();
    }

    public UserResponseDTO toUserResponseDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId().toString())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    public AuthInfoDTO toAuthInfoDTO(User user) {
        return AuthInfoDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .subscriptionPlan(user.getSubscriptionPlan())
                .active(user.getActive())
                .avatarUrl(user.getAvatarUrl())
                .voiceRequestsToday(user.getVoiceRequestsToday())
                .canUseVoice(user.canUseVoice())
                .createdAt(user.getCreatedAt())
                .build();
    }
}