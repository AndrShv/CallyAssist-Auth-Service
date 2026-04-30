package com.example.project.entity;

import com.example.project.enums.Role;
import com.example.project.enums.SubscriptionPlan;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", columnDefinition = "BINARY(16)", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "username", unique = true)
    private String username;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_plan", nullable = false)
    @Builder.Default
    private SubscriptionPlan subscriptionPlan = SubscriptionPlan.FREE;

    @Column(name = "subscription_expires_at")
    private LocalDateTime subscriptionExpiresAt;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "voice_requests_today", nullable = false)
    @Builder.Default
    private Integer voiceRequestsToday = 0;

    @Column(name = "voice_requests_reset_date")
    private LocalDate voiceRequestsResetDate;

    @Column(name = "reset_token")
    private String resetToken;

    @Column(name = "reset_token_created_at")
    private LocalDateTime resetTokenCreatedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.active == null)               this.active = true;
        if (this.subscriptionPlan == null)     this.subscriptionPlan = SubscriptionPlan.FREE;
        if (this.voiceRequestsToday == null)   this.voiceRequestsToday = 0;
        if (this.voiceRequestsResetDate == null) this.voiceRequestsResetDate = LocalDate.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ── бизнес-логика ─────────────────────────────────────────────

    public boolean hasActivePaidSubscription() {
        if (subscriptionPlan == null || subscriptionPlan == SubscriptionPlan.FREE) return false;
        if (subscriptionExpiresAt == null) return false;
        return subscriptionExpiresAt.isAfter(LocalDateTime.now());
    }

    public void resetVoiceCounterIfNewDay() {
        LocalDate today = LocalDate.now();
        if (voiceRequestsResetDate == null || !voiceRequestsResetDate.equals(today)) {
            this.voiceRequestsToday = 0;
            this.voiceRequestsResetDate = today;
        }
    }

    public boolean canUseVoice() {
        if (role == Role.ADMIN) return true;
        if (hasActivePaidSubscription()) return true;
        resetVoiceCounterIfNewDay();
        return voiceRequestsToday < 10;
    }

    public void incrementVoiceRequests() {
        resetVoiceCounterIfNewDay();
        this.voiceRequestsToday++;
    }
}
