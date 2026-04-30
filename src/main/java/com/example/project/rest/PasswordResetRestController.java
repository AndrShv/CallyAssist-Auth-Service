package com.example.project.rest;


import com.example.project.dto.ForgotPasswordDTO;
import com.example.project.dto.ResetPasswordDTO;
import com.example.project.interfaces.ResetPassword;
import com.example.project.interfaces.SendResetToken;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/password/reset")
@RequiredArgsConstructor
public class PasswordResetRestController {

    private final SendResetToken sendResetToken;
    private final ResetPassword resetPassword;

    @PostMapping("/request")
    public ResponseEntity<?> requestResetToken(@Valid @RequestBody ForgotPasswordDTO dto) {
        log.info("REST: Запрос на сброс пароля для email={}", dto.getEmail());

        sendResetToken.sendResetToken(dto.getEmail());

        return ResponseEntity.ok(
                "Код для сброса пароля отправлен на email"
        );
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirmResetPassword(@Valid @RequestBody ResetPasswordDTO dto) {
        log.info("REST: Попытка сброса пароля по токену");

        resetPassword.resetPassword(dto.getToken(), dto.getNewPassword());

        return ResponseEntity.ok(
                "Пароль успешно изменён"
        );

    }


}

