package com.example.project.rest;

import com.example.project.config.JwtAuthenticationEntryPoint;
import com.example.project.config.SecurityConfig;
import com.example.project.exceptions.EmailSendingExeption;
import com.example.project.filter.JwtFilter;
import com.example.project.handlers.OAuth2SuccessHandler;
import com.example.project.interfaces.EmailService;
import com.example.project.util.JwtUtil;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmailRestController.class)
@Import(SecurityConfig.class)
class EmailRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmailService emailService;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockBean
    private JwtFilter jwtFilter;

    @MockBean
    private OAuth2SuccessHandler oAuth2SuccessHandler;


    @MockBean
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(inv -> {
            FilterChain chain = inv.getArgument(2);
            chain.doFilter(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(jwtFilter).doFilter(any(), any(), any());

        doAnswer(inv -> {
            jakarta.servlet.http.HttpServletResponse response = inv.getArgument(1);
            response.sendError(jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            return null;
        }).when(jwtAuthenticationEntryPoint).commence(any(), any(), any());
    }

    // ============================
    // POST /api/email/send-code
    // ============================

    @Test
    @WithMockUser(roles = "ADMIN")
    void sendCode_success() throws Exception {
        doNothing().when(emailService)
                .sendPasswordResetCode("andrey@test.com", "123456");

        mockMvc.perform(post("/api/email/send-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "to": "andrey@test.com",
                                  "code": "123456"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string("Письмо отправлено на andrey@test.com"));

        verify(emailService).sendPasswordResetCode("andrey@test.com", "123456");
    }

    @Test
    @WithMockUser(roles = "USER")
    void sendCode_forbidden_forUser() throws Exception {
        mockMvc.perform(post("/api/email/send-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "to": "andrey@test.com",
                                  "code": "123456"
                                }
                                """))
                .andExpect(status().isForbidden());

        verify(emailService, never()).sendPasswordResetCode(any(), any());
    }

    @Test
    void sendCode_unauthorized_withoutAuth() throws Exception {
        mockMvc.perform(post("/api/email/send-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "to": "andrey@test.com",
                                  "code": "123456"
                                }
                                """))
                .andExpect(status().isUnauthorized());

        verify(emailService, never()).sendPasswordResetCode(any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void sendCode_invalidEmail_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/email/send-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "to": "not-an-email",
                                  "code": "123456"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(emailService, never()).sendPasswordResetCode(any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void sendCode_blankCode_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/email/send-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "to": "andrey@test.com",
                                  "code": ""
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(emailService, never()).sendPasswordResetCode(any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void sendCode_blankEmail_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/email/send-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "to": "",
                                  "code": "123456"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(emailService, never()).sendPasswordResetCode(any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void sendCode_emailServiceThrows_shouldReturn500() throws Exception {
        doThrow(new EmailSendingExeption("SMTP error", new RuntimeException()))
                .when(emailService).sendPasswordResetCode(anyString(), anyString());

        mockMvc.perform(post("/api/email/send-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "to": "andrey@test.com",
                                  "code": "123456"
                                }
                                """))
                .andExpect(status().isInternalServerError());
    }
}