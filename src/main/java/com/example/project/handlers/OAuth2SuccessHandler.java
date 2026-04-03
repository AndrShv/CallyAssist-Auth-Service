package com.example.project.handlers;

import com.example.project.interfaces.GenerateTokenForOAuth2;
import com.example.project.services.custom.CustomOidcUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    
    private final GenerateTokenForOAuth2 generateTokenForOAuth2;

    @Value("${app.oauth2.redirect-uri:http://localhost:8081/api/auth/oauth2/token}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        CustomOidcUser oidcUser = (CustomOidcUser) authentication.getPrincipal();
        String token = generateTokenForOAuth2.generateTokenForOAuth2(oidcUser.getUser().getId());

        log.info("OAuth2 success: {} → JWT generated", oidcUser.getEmail());

        String url = redirectUri + "?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
        getRedirectStrategy().sendRedirect(request, response, url);
    }
}
