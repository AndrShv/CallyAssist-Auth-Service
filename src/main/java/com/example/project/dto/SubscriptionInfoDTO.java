package com.example.project.dto;

import com.example.project.enums.SubscriptionPlan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionInfoDTO {

    private SubscriptionPlan subscriptionPlan;
    private LocalDateTime subscriptionExpiresAt;
    private Boolean active;
}