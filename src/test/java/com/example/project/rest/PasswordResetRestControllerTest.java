package com.example.project.rest;

import com.example.project.dto.ForgotPasswordDTO;
import com.example.project.dto.ResetPasswordDTO;
import com.example.project.exceptions.EmailException;
import com.example.project.filter.JwtFilter;
import com.example.project.interfaces.ResetPassword;
import com.example.project.interfaces.SendResetToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = PasswordResetRestController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtFilter.class
        )
)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
        "server.port=8081"
})
class PasswordResetRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SendResetToken sendResetToken;

    @MockBean
    private ResetPassword resetPassword;

    // ===============================
    // REQUEST RESET TOKEN
    // ===============================

    @Test
    void requestResetToken_success() throws Exception {
        ForgotPasswordDTO dto = new ForgotPasswordDTO();
        dto.setEmail("test@example.com");

        Mockito.when(sendResetToken.sendResetToken(anyString())).thenReturn("123456");

        mockMvc.perform(post("/api/password/reset/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Код для сброса пароля отправлен на email"));

        Mockito.verify(sendResetToken).sendResetToken("test@example.com");
    }

    @Test
    void requestResetToken_invalidEmail_shouldReturn400() throws Exception {
        ForgotPasswordDTO dto = new ForgotPasswordDTO();
        dto.setEmail("invalid-email");

        mockMvc.perform(post("/api/password/reset/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void requestResetToken_exception_shouldReturn500() throws Exception {
        ForgotPasswordDTO dto = new ForgotPasswordDTO();
        dto.setEmail("test@example.com");

        Mockito.doThrow(new EmailException("Email service error"))
                .when(sendResetToken).sendResetToken(anyString());

        mockMvc.perform(post("/api/password/reset/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"));
    }

    // ===============================
    // CONFIRM RESET PASSWORD
    // ===============================

    @Test
    void confirmResetPassword_success() throws Exception {
        ResetPasswordDTO dto = new ResetPasswordDTO();
        dto.setToken("123456");
        dto.setNewPassword("newPassword123");

        Mockito.doNothing().when(resetPassword).resetPassword(anyString(), anyString());

        mockMvc.perform(post("/api/password/reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Пароль успешно изменён"));

        Mockito.verify(resetPassword).resetPassword("123456", "newPassword123");
    }

    @Test
    void confirmResetPassword_missingToken_shouldReturn400() throws Exception {
        ResetPasswordDTO dto = new ResetPasswordDTO();
        dto.setNewPassword("newPassword123");

        mockMvc.perform(post("/api/password/reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void confirmResetPassword_missingPassword_shouldReturn400() throws Exception {
        ResetPasswordDTO dto = new ResetPasswordDTO();
        dto.setToken("123456");

        mockMvc.perform(post("/api/password/reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void confirmResetPassword_exception_shouldReturn500() throws Exception {
        ResetPasswordDTO dto = new ResetPasswordDTO();
        dto.setToken("123456");
        dto.setNewPassword("newPassword123");

        Mockito.doThrow(new RuntimeException("Token invalid or expired"))
                .when(resetPassword).resetPassword(anyString(), anyString());

        mockMvc.perform(post("/api/password/reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"));
    }
    @Test
    void confirmResetPassword_emptyBody_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/password/reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

}

