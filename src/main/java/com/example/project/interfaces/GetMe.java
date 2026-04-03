package com.example.project.interfaces;

import com.example.project.dto.AuthInfoDTO;

import java.util.UUID;

public interface GetMe {
    AuthInfoDTO getMe(UUID userId);

}
