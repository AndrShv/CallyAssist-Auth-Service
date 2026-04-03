package com.example.project.interfaces;

import com.example.project.dto.UserLoginDTO;
import com.example.project.dto.UserResponseDTO;

public interface Login {
    UserResponseDTO login(UserLoginDTO dto);

}
