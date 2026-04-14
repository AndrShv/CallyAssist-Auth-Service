package com.example.project.services.custom;

import com.example.project.entity.User;
import com.example.project.enums.Role;
import com.example.project.enums.SubscriptionPlan;
import com.example.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends OidcUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);
        String email = oidcUser.getEmail();

        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        log.info("OAuth2 login: {}", normalizedEmail);

        Optional<User> existingOpt = userRepository.findByEmailIgnoreCase(normalizedEmail);
        User user;

        if (existingOpt.isPresent()) {
            user = existingOpt.get();
            user = updateExisting(user, oidcUser);

        } else {
            user = createNew(normalizedEmail, oidcUser);
        }

        return new CustomOidcUser(oidcUser, user);
    }

    private User updateExisting(User user, OidcUser oidcUser) {
        boolean dirty = false;

        String oauthName = oidcUser.getFullName();
        if (oauthName != null && !oauthName.isBlank()) {
            if (user.getUsername() == null || user.getUsername().isBlank()
                    || user.getUsername().equals(user.getEmail())
                    || user.getUsername().startsWith("oauth2-")) {
                user.setUsername(oauthName);
                dirty = true;
            }
        }

        String picture = oidcUser.getPicture();
        if (picture != null && !picture.equals(user.getAvatarUrl())) {
            user.setAvatarUrl(picture);
            dirty = true;
        }

        if (!Boolean.TRUE.equals(user.getActive())) {
            user.setActive(true);
            dirty = true;
        }

        if (dirty) {
            user = userRepository.save(user);
            log.info("User updated from OAuth2: {}", user.getEmail());
        }

        return user;
    }

    private User createNew(String email, OidcUser oidcUser) {
        String oauthName = oidcUser.getFullName();
        String username  = (oauthName != null && !oauthName.isBlank()) ? oauthName : email;
        String picture   = oidcUser.getPicture();

        User newUser = User.builder()
                .email(email)
                .username(username)
                .password(passwordEncoder.encode(UUID.randomUUID().toString())) // случайный пароль
                .role(Role.USER)
                .subscriptionPlan(SubscriptionPlan.FREE)
                .avatarUrl(picture)
                .active(true)
                .voiceRequestsToday(0)
                .build();

        newUser = userRepository.save(newUser);
        log.info("New user created from OAuth2: {} ({})", newUser.getEmail(), newUser.getId());
        return newUser;
    }
}