package com.example.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoiceRequestDTO {

    @NotBlank(message = "Текст команды обязателен")
    @Size(max = 2000)
    private String text;

    @Builder.Default
    private String language = "ru-RU";
}