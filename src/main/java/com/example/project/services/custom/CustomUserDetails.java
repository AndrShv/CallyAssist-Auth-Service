package com.example.project.services.custom;

import com.example.project.entity.User;
import com.example.project.enums.Role;
import com.example.project.enums.SubscriptionPlan;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    // ── все поля пользователя ─────────────────────────────────────
    // Используй так: ((CustomUserDetails) authentication.getPrincipal()).getId()
    // НО: JwtFilter ставит principal = userId.toString(), поэтому
    // для получения userId из Authentication используй:
    // UUID.fromString(authentication.getPrincipal().toString())

    public UUID getId()                          { return user.getId(); }
    public String getEmail()                     { return user.getEmail(); }
    public Role getRole()                        { return user.getRole(); }
    public Boolean getActive()                   { return user.getActive(); }
    public SubscriptionPlan getSubscriptionPlan(){ return user.getSubscriptionPlan(); }
    public String getAvatarUrl()                 { return user.getAvatarUrl(); }
    public Integer getVoiceRequestsToday()       { return user.getVoiceRequestsToday(); }
    public boolean canUseVoice()                 { return user.canUseVoice(); }
    public LocalDateTime getCreatedAt()          { return user.getCreatedAt(); }
    public User getUser()                        { return user; }

    // ── UserDetails ───────────────────────────────────────────────

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(() -> "ROLE_" + user.getRole().name());
    }

    @Override public String getPassword()              { return user.getPassword(); }
    @Override public String getUsername()              { return user.getEmail(); }
    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()               { return Boolean.TRUE.equals(user.getActive()); }
}
