package com.example.project.services;

import com.example.project.dto.*;
import com.example.project.entity.User;
import com.example.project.enums.Role;
import com.example.project.enums.SubscriptionPlan;
import com.example.project.exceptions.UserAlreadyExistsException;
import com.example.project.exceptions.UserNotFoundByIDException;
import com.example.project.mappers.UserMapper;
import com.example.project.repository.UserRepository;
import com.example.project.services.custom.CustomUserDetails;
import com.example.project.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private UserMapper userMapper;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private Authentication authentication;

    @InjectMocks
    private AuthServiceImpl authService;

    private UserRegisterDTO registerDTO;
    private UserLoginDTO loginDTO;
    private User user;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        registerDTO = new UserRegisterDTO();
        registerDTO.setEmail("  ANDREY@mail.com  ");
        registerDTO.setUsername(" Andrey ");
        registerDTO.setPassword("pass123");
        registerDTO.setRole("USER");

        loginDTO = new UserLoginDTO();
        loginDTO.setEmail("ANDREY@mail.com");
        loginDTO.setPassword("pass123");

        user = User.builder()
                .id(userId)
                .email("andrey@mail.com")
                .username("Andrey")
                .role(Role.USER)
                .subscriptionPlan(SubscriptionPlan.FREE)
                .active(true)
                .build();
    }

    @Nested
    @DisplayName("register() scenarios")
    class RegisterTests {

        @Test
        @DisplayName("1. Success registration with normalization")
        void successRegistration() {
            when(userRepository.existsByEmailIgnoreCase("andrey@mail.com")).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("hashed");
            when(userRepository.save(any(User.class))).thenReturn(user);
            when(userMapper.toUserResponseDTO(any(), eq(null))).thenReturn(new UserResponseDTO());

            authService.register(registerDTO);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            assertEquals("andrey@mail.com", userCaptor.getValue().getEmail());
            assertEquals("Andrey", userCaptor.getValue().getUsername());
        }

        @Test
        @DisplayName("2. Fail when email already exists")
        void failIfUserExists() {
            when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(true);
            assertThrows(UserAlreadyExistsException.class, () -> authService.register(registerDTO));
        }

        @Test
        @DisplayName("3. Use default role if role is null")
        void defaultRoleWhenNull() {
            registerDTO.setRole(null);
            when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
            when(userRepository.save(any())).thenReturn(user);

            authService.register(registerDTO);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            assertEquals(Role.USER, userCaptor.getValue().getRole());
        }

        @Test
        @DisplayName("4. Use default role if role name is invalid")
        void defaultRoleWhenInvalid() {
            registerDTO.setRole("GOD_MODE");
            when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
            when(userRepository.save(any())).thenReturn(user);

            authService.register(registerDTO);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            assertEquals(Role.USER, userCaptor.getValue().getRole());
        }

        @Test
        @DisplayName("5. Correct mapping to ResponseDTO")
        void mappingCheck() {
            UserResponseDTO dto = UserResponseDTO.builder().email("andrey@mail.com").build();
            when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
            when(userRepository.save(any())).thenReturn(user);
            when(userMapper.toUserResponseDTO(user, null)).thenReturn(dto);

            UserResponseDTO result = authService.register(registerDTO);
            assertEquals("andrey@mail.com", result.getEmail());
        }
    }

    @Nested
    @DisplayName("login() scenarios")
    class LoginTests {

        @Test
        @DisplayName("1. Success login")
        void successLogin() {
            CustomUserDetails details = mock(CustomUserDetails.class);
            when(userRepository.findByEmailIgnoreCase("andrey@mail.com")).thenReturn(Optional.of(user));
            user.setPassword("hashed"); // Пароль не пустой

            when(authenticationManager.authenticate(any())).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(details);
            when(details.getUser()).thenReturn(user);
            when(jwtUtil.generateToken(anyString(), any(), any())).thenReturn("token");
            when(userMapper.toUserResponseDTO(any(), anyString())).thenReturn(new UserResponseDTO());

            authService.login(loginDTO);

            verify(jwtUtil).generateToken(eq(user.getEmail()), eq(user.getId()), any());
        }

        @Test
        @DisplayName("2. Fail login - user not found")
        void userNotFound() {
            when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());
            assertThrows(UsernameNotFoundException.class, () -> authService.login(loginDTO));
        }

        @Test
        @DisplayName("3. Fail login - OAuth2 user (no password)")
        void oauth2UserLoginFail() {
            user.setPassword("");
            when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(user));

            RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login(loginDTO));
            assertTrue(ex.getMessage().contains("OAuth2"));
        }

        @Test
        @DisplayName("4. Fail login - Wrong credentials")
        void wrongCredentials() {
            when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(user));
            user.setPassword("hashed");
            when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Wrong"));

            assertThrows(BadCredentialsException.class, () -> authService.login(loginDTO));
        }
    }

    @Nested
    @DisplayName("generateTokenForOAuth2() scenarios")
    class OAuthTokenTests {

        @Test
        @DisplayName("1. Success token generation")
        void successOAuthToken() {
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(jwtUtil.generateToken(anyString(), any(), any())).thenReturn("oa-token");

            String token = authService.generateTokenForOAuth2(userId);
            assertEquals("oa-token", token);
        }

        @Test
        @DisplayName("2. Fail if user id not found")
        void idNotFound() {
            when(userRepository.findById(any())).thenReturn(Optional.empty());
            assertThrows(UserNotFoundByIDException.class, () -> authService.generateTokenForOAuth2(userId));
        }
    }

    @Nested
    @DisplayName("getMe() & Subscription scenarios")
    class InfoTests {

        @Test
        @DisplayName("1. getMe - Success")
        void getMeSuccess() {
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userMapper.toAuthInfoDTO(user)).thenReturn(new AuthInfoDTO());
            assertNotNull(authService.getMe(userId));
        }

        @Test
        @DisplayName("2. getMe - User Not Found")
        void getMeFail() {
            when(userRepository.findById(userId)).thenReturn(Optional.empty());
            assertThrows(UserNotFoundByIDException.class, () -> authService.getMe(userId));
        }

        @Test
        @DisplayName("3. getSubscription - Success data builder")
        void subscriptionSuccess() {
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            SubscriptionInfoDTO info = authService.getSubscriptionInfo(userId);
            assertEquals(SubscriptionPlan.FREE, info.getSubscriptionPlan());
            assertTrue(info.getActive());
        }
    }
}