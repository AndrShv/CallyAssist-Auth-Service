package com.example.project.services.custom;

import com.example.project.entity.User;
import com.example.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        String normalized = identifier.trim().toLowerCase(Locale.ROOT);

        Optional<User> userOpt = userRepository.findByEmailIgnoreCase(normalized);
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByUsernameIgnoreCase(normalized);
        }

        User user = userOpt.orElseThrow(() ->
                new UsernameNotFoundException("Пользователь не найден: " + identifier));

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new UsernameNotFoundException("Пользователь неактивен: " + identifier);
        }

        log.debug("User loaded: {} ({})", user.getEmail(), user.getId());
        return new CustomUserDetails(user);
    }
}
