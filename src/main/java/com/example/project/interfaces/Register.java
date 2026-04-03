package com.example.project.interfaces;

import com.example.project.dto.UserRegisterDTO;
import com.example.project.dto.UserResponseDTO;

public interface Register {
    UserResponseDTO register(UserRegisterDTO dto);

}
