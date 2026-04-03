package com.example.project.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendCodeRequest {

    @Email(message = "Некорректный email")
    @NotBlank(message = "Email обязателен")
    String to;

    @NotBlank(message = "Код обязателен")
    String code;
}
