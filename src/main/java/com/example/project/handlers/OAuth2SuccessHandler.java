package com.example.project.handlers;

import com.example.project.interfaces.GenerateTokenForOAuth2;
import com.example.project.services.custom.CustomOidcUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final GenerateTokenForOAuth2 generateTokenForOAuth2;
    private final String redirectUri;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        CustomOidcUser user = (CustomOidcUser) authentication.getPrincipal();

        UUID userId = user.getUser().getId();

        String jwt = generateTokenForOAuth2.generateTokenForOAuth2(userId);

        log.info("OAuth2 login success: {}", user.getEmail());

        String redirectUrl =
                redirectUri + "?token=" + URLEncoder.encode(jwt, StandardCharsets.UTF_8);

        response.sendRedirect(redirectUrl);
    }
}