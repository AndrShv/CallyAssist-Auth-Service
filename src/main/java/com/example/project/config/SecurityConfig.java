package com.example.project.config;

import com.example.project.filter.JwtFilter;
import com.example.project.handlers.OAuth2SuccessHandler;
import com.example.project.interfaces.GenerateTokenForOAuth2;
import com.example.project.repository.HttpCookieOAuth2AuthorizationRequestRepository;
import com.example.project.services.custom.CustomOAuth2UserService;
import com.example.project.services.custom.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final CustomUserDetailsService userDetailsService;
    private final CustomOAuth2UserService oAuth2UserService;
    private final PasswordEncoder passwordEncoder;
    private final HttpCookieOAuth2AuthorizationRequestRepository cookieRepository;

    @Value("${app.oauth2.redirect-uri:com.andrshv.cally://oauth}")
    private String redirectUri;

    private static final String[] PUBLIC = {
            "/api/auth/subscription/**",
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/oauth2/token",
            "/api/password/reset/**",
            "/oauth2/**",
            "/login/oauth2/**",
            "/actuator/**",
            "/swagger-ui/**",
            "/v3/api-docs/**"
    };

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            OAuth2SuccessHandler oAuth2SuccessHandler
    ) throws Exception {

        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC).permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/email/send-code").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )

                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(auth ->
                                auth.authorizationRequestRepository(cookieRepository
                                )
                        )
                        .userInfoEndpoint(ui ->
                                ui.oidcUserService(oAuth2UserService)
                        )
                        .successHandler(oAuth2SuccessHandler)
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public OAuth2SuccessHandler oAuth2SuccessHandler(
            GenerateTokenForOAuth2 generateTokenForOAuth2
    ) {
        return new OAuth2SuccessHandler(generateTokenForOAuth2, redirectUri);
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        var provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

}