package com.example.project.interfaces;

import com.example.project.dto.SubscriptionInfoDTO;

import java.util.UUID;

public interface GetSubscriptionInfo {
    SubscriptionInfoDTO getSubscriptionInfo(UUID userId);
}
