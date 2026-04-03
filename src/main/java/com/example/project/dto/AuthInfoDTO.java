package com.example.project.dto;

import com.example.project.enums.Role;
import com.example.project.enums.SubscriptionPlan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthInfoDTO {
    private UUID id;
    private String username;
    private String email;
    private Role role;
    private SubscriptionPlan subscriptionPlan;
    private Boolean active;
    private String avatarUrl;
    private Integer voiceRequestsToday;
    private Boolean canUseVoice;
    private LocalDateTime createdAt;
}