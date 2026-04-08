package com.example.project.services;

import com.example.project.dto.AuthInfoDTO;
import com.example.project.dto.UserLoginDTO;
import com.example.project.dto.UserRegisterDTO;
import com.example.project.dto.UserResponseDTO;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserMapper userMapper;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private Authentication authentication;

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
        registerDTO.setEmail("  TEST@MAIL.COM  ");
        registerDTO.setUsername("Andrew");
        registerDTO.setPassword("password123");
        registerDTO.setRole("USER");

        loginDTO = new UserLoginDTO();
        loginDTO.setEmail("  TEST@MAIL.COM  ");
        loginDTO.setPassword("password123");

        user = User.builder()
                .id(userId)
                .email("test@mail.com")
                .username("Andrew")
                .password("encodedPassword")
                .role(Role.USER)
                .subscriptionPlan(SubscriptionPlan.FREE)
                .active(true)
                .voiceRequestsToday(0)
                .build();
    }

    @Nested
    @DisplayName("register() tests")
    class RegisterTests {

        @Test
        @DisplayName("should register user successfully")
        void shouldRegisterUserSuccessfully() {
            UserResponseDTO expectedResponse = UserResponseDTO.builder()
                    .id(userId.toString())
                    .email(user.getEmail())
                    .username(user.getUsername())
                    .role(user.getRole().name())
                    .token("jwt-token")
                    .build();

            when(userRepository.existsByEmailIgnoreCase("test@mail.com")).thenReturn(false);
            when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(user);
            when(jwtUtil.generateToken(eq(user.getEmail()), eq(user.getId()), eq(List.of(user.getRole()))))
                    .thenReturn("jwt-token");
            when(userMapper.toUserResponseDTO(user, "jwt-token")).thenReturn(expectedResponse);

            UserResponseDTO actual = authService.register(registerDTO);

            assertNotNull(actual);
            assertEquals(expectedResponse, actual);

            verify(userRepository).existsByEmailIgnoreCase("test@mail.com");
            verify(passwordEncoder).encode("password123");
            verify(userRepository).save(any(User.class));
            verify(jwtUtil).generateToken(user.getEmail(), user.getId(), List.of(user.getRole()));
            verify(userMapper).toUserResponseDTO(user, "jwt-token");
        }

        @Test
        @DisplayName("should normalize email before registration")
        void shouldNormalizeEmailBeforeRegistration() {
            when(userRepository.existsByEmailIgnoreCase("test@mail.com")).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(user);
            when(jwtUtil.generateToken(anyString(), any(UUID.class), anyList())).thenReturn("jwt-token");
            when(userMapper.toUserResponseDTO(any(User.class), eq("jwt-token")))
                    .thenReturn(UserResponseDTO.builder().build());

            authService.register(registerDTO);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());

            User savedUser = userCaptor.getValue();
            assertEquals("test@mail.com", savedUser.getEmail());
        }

        @Test
        @DisplayName("should throw exception when user already exists")
        void shouldThrowWhenUserAlreadyExists() {
            when(userRepository.existsByEmailIgnoreCase("test@mail.com")).thenReturn(true);

            UserAlreadyExistsException ex = assertThrows(
                    UserAlreadyExistsException.class,
                    () -> authService.register(registerDTO)
            );

            assertEquals("Пользователь уже существует", ex.getMessage());

            verify(userRepository).existsByEmailIgnoreCase("test@mail.com");
            verify(userRepository, never()).save(any());
            verify(passwordEncoder, never()).encode(anyString());
            verify(jwtUtil, never()).generateToken(anyString(), any(), anyList());
        }

        @Test
        @DisplayName("should set USER role when dto role is null")
        void shouldSetDefaultUserRoleWhenRoleIsNull() {
            registerDTO.setRole(null);

            when(userRepository.existsByEmailIgnoreCase("test@mail.com")).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(user);
            when(jwtUtil.generateToken(anyString(), any(UUID.class), anyList())).thenReturn("jwt-token");
            when(userMapper.toUserResponseDTO(any(User.class), eq("jwt-token")))
                    .thenReturn(UserResponseDTO.builder().build());

            authService.register(registerDTO);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());

            assertEquals(Role.USER, userCaptor.getValue().getRole());
        }

        @Test
        @DisplayName("should set USER role when dto role is invalid")
        void shouldSetDefaultUserRoleWhenRoleIsInvalid() {
            registerDTO.setRole("INVALID_ROLE");

            when(userRepository.existsByEmailIgnoreCase("test@mail.com")).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(user);
            when(jwtUtil.generateToken(anyString(), any(UUID.class), anyList())).thenReturn("jwt-token");
            when(userMapper.toUserResponseDTO(any(User.class), eq("jwt-token")))
                    .thenReturn(UserResponseDTO.builder().build());

            authService.register(registerDTO);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());

            assertEquals(Role.USER, userCaptor.getValue().getRole());
        }

        @Nested
        @DisplayName("login() tests")
        class LoginTests {

            @Test
            @DisplayName("should login successfully")
            void shouldLoginSuccessfully() {
                CustomUserDetails customUserDetails = mock(CustomUserDetails.class);
                UserResponseDTO expectedResponse = UserResponseDTO.builder()
                        .id(userId.toString())
                        .email(user.getEmail())
                        .username(user.getUsername())
                        .role(user.getRole().name())
                        .token("jwt-token")
                        .build();

                when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                        .thenReturn(authentication);
                when(authentication.getPrincipal()).thenReturn(customUserDetails);
                when(customUserDetails.getUser()).thenReturn(user);
                when(jwtUtil.generateToken(user.getEmail(), user.getId(), List.of(user.getRole())))
                        .thenReturn("jwt-token");
                when(userMapper.toUserResponseDTO(user, "jwt-token")).thenReturn(expectedResponse);

                UserResponseDTO actual = authService.login(loginDTO);

                assertNotNull(actual);
                assertEquals(expectedResponse, actual);

                verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
                verify(jwtUtil).generateToken(user.getEmail(), user.getId(), List.of(user.getRole()));
                verify(userMapper).toUserResponseDTO(user, "jwt-token");
            }

            @Test
            @DisplayName("should normalize email before login")
            void shouldNormalizeEmailBeforeLogin() {
                CustomUserDetails customUserDetails = mock(CustomUserDetails.class);

                when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                        .thenReturn(authentication);
                when(authentication.getPrincipal()).thenReturn(customUserDetails);
                when(customUserDetails.getUser()).thenReturn(user);
                when(jwtUtil.generateToken(anyString(), any(UUID.class), anyList())).thenReturn("jwt-token");
                when(userMapper.toUserResponseDTO(any(User.class), eq("jwt-token")))
                        .thenReturn(UserResponseDTO.builder().build());

                authService.login(loginDTO);

                ArgumentCaptor<UsernamePasswordAuthenticationToken> captor =
                        ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);

                verify(authenticationManager).authenticate(captor.capture());

                UsernamePasswordAuthenticationToken token = captor.getValue();
                assertEquals("test@mail.com", token.getPrincipal());
                assertEquals("password123", token.getCredentials());
            }

            @Test
            @DisplayName("should generate token after successful login")
            void shouldGenerateTokenAfterLogin() {
                CustomUserDetails customUserDetails = mock(CustomUserDetails.class);

                when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                        .thenReturn(authentication);
                when(authentication.getPrincipal()).thenReturn(customUserDetails);
                when(customUserDetails.getUser()).thenReturn(user);
                when(jwtUtil.generateToken(user.getEmail(), user.getId(), List.of(user.getRole())))
                        .thenReturn("jwt-token");
                when(userMapper.toUserResponseDTO(any(User.class), eq("jwt-token")))
                        .thenReturn(UserResponseDTO.builder().token("jwt-token").build());

                UserResponseDTO response = authService.login(loginDTO);

                assertEquals("jwt-token", response.getToken());
                verify(jwtUtil).generateToken(user.getEmail(), user.getId(), List.of(user.getRole()));
            }

            @Test
            @DisplayName("should use authenticationManager during login")
            void shouldUseAuthenticationManagerDuringLogin() {
                CustomUserDetails customUserDetails = mock(CustomUserDetails.class);

                when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                        .thenReturn(authentication);
                when(authentication.getPrincipal()).thenReturn(customUserDetails);
                when(customUserDetails.getUser()).thenReturn(user);
                when(jwtUtil.generateToken(anyString(), any(UUID.class), anyList())).thenReturn("jwt-token");
                when(userMapper.toUserResponseDTO(any(User.class), eq("jwt-token")))
                        .thenReturn(UserResponseDTO.builder().build());

                authService.login(loginDTO);

                verify(authenticationManager, times(1))
                        .authenticate(any(UsernamePasswordAuthenticationToken.class));
            }

            @Test
            @DisplayName("should throw exception when authentication fails")
            void shouldThrowWhenAuthenticationFails() {
                when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                        .thenThrow(new BadCredentialsException("Bad credentials"));

                assertThrows(BadCredentialsException.class, () -> authService.login(loginDTO));

                verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
                verify(jwtUtil, never()).generateToken(anyString(), any(), anyList());
                verify(userMapper, never()).toUserResponseDTO(any(), anyString());
            }
        }

        @Nested
        @DisplayName("generateTokenForOAuth2() tests")
        class GenerateTokenForOAuth2Tests {

            @Test
            @DisplayName("should generate token successfully for existing user")
            void shouldGenerateTokenSuccessfully() {
                when(userRepository.findById(userId)).thenReturn(Optional.of(user));
                when(jwtUtil.generateToken(user.getEmail(), user.getId(), List.of(user.getRole())))
                        .thenReturn("oauth2-jwt-token");

                String token = authService.generateTokenForOAuth2(userId);

                assertNotNull(token);
                assertEquals("oauth2-jwt-token", token);

                verify(userRepository).findById(userId);
                verify(jwtUtil).generateToken(user.getEmail(), user.getId(), List.of(user.getRole()));
            }

            @Test
            @DisplayName("should find user by id before generating token")
            void shouldFindUserByIdBeforeGeneratingToken() {
                when(userRepository.findById(userId)).thenReturn(Optional.of(user));
                when(jwtUtil.generateToken(anyString(), any(UUID.class), anyList())).thenReturn("token");

                authService.generateTokenForOAuth2(userId);

                verify(userRepository, times(1)).findById(userId);
            }

            @Test
            @DisplayName("should throw exception when user not found for oauth2 token")
            void shouldThrowWhenUserNotFoundForOAuth2Token() {
                when(userRepository.findById(userId)).thenReturn(Optional.empty());

                UserNotFoundByIDException ex = assertThrows(
                        UserNotFoundByIDException.class,
                        () -> authService.generateTokenForOAuth2(userId)
                );

                assertEquals("User not found: " + userId, ex.getMessage());

                verify(userRepository).findById(userId);
                verify(jwtUtil, never()).generateToken(anyString(), any(), anyList());
            }

            @Test
            @DisplayName("should pass correct user data to jwt util")
            void shouldPassCorrectUserDataToJwtUtil() {
                when(userRepository.findById(userId)).thenReturn(Optional.of(user));
                when(jwtUtil.generateToken(anyString(), any(UUID.class), anyList())).thenReturn("token");

                authService.generateTokenForOAuth2(userId);

                verify(jwtUtil).generateToken(
                        eq(user.getEmail()),
                        eq(user.getId()),
                        eq(List.of(user.getRole()))
                );
            }

            @Test
            @DisplayName("should return exact token from jwt util")
            void shouldReturnExactTokenFromJwtUtil() {
                when(userRepository.findById(userId)).thenReturn(Optional.of(user));
                when(jwtUtil.generateToken(anyString(), any(UUID.class), anyList())).thenReturn("MY_EXACT_TOKEN");

                String result = authService.generateTokenForOAuth2(userId);

                assertEquals("MY_EXACT_TOKEN", result);
            }
        }

        @Nested
        @DisplayName("getMe() tests")
        class GetMeTests {

            @Test
            @DisplayName("should return auth info successfully")
            void shouldReturnAuthInfoSuccessfully() {
                AuthInfoDTO expectedDto = new AuthInfoDTO();
                expectedDto.setId(userId);
                expectedDto.setEmail(user.getEmail());
                expectedDto.setUsername(user.getUsername());

                when(userRepository.findById(userId)).thenReturn(Optional.of(user));
                when(userMapper.toAuthInfoDTO(user)).thenReturn(expectedDto);

                AuthInfoDTO actual = authService.getMe(userId);

                assertNotNull(actual);
                assertEquals(expectedDto, actual);

                verify(userRepository).findById(userId);
                verify(userMapper).toAuthInfoDTO(user);
            }

            @Test
            @DisplayName("should call repository findById in getMe")
            void shouldCallFindByIdInGetMe() {
                when(userRepository.findById(userId)).thenReturn(Optional.of(user));
                when(userMapper.toAuthInfoDTO(user)).thenReturn(new AuthInfoDTO());

                authService.getMe(userId);

                verify(userRepository, times(1)).findById(userId);
            }

            @Test
            @DisplayName("should throw exception when user not found in getMe")
            void shouldThrowWhenUserNotFoundInGetMe() {
                when(userRepository.findById(userId)).thenReturn(Optional.empty());

                UserNotFoundByIDException ex = assertThrows(
                        UserNotFoundByIDException.class,
                        () -> authService.getMe(userId)
                );

                assertEquals("User not found: " + userId, ex.getMessage());

                verify(userRepository).findById(userId);
                verify(userMapper, never()).toAuthInfoDTO(any());
            }

            @Test
            @DisplayName("should use mapper to convert user to auth info dto")
            void shouldUseMapperInGetMe() {
                AuthInfoDTO dto = new AuthInfoDTO();

                when(userRepository.findById(userId)).thenReturn(Optional.of(user));
                when(userMapper.toAuthInfoDTO(user)).thenReturn(dto);

                authService.getMe(userId);

                verify(userMapper, times(1)).toAuthInfoDTO(user);
            }

            @Test
            @DisplayName("should return exact dto from mapper")
            void shouldReturnExactDtoFromMapper() {
                AuthInfoDTO dto = new AuthInfoDTO();
                dto.setEmail("mapped@mail.com");

                when(userRepository.findById(userId)).thenReturn(Optional.of(user));
                when(userMapper.toAuthInfoDTO(user)).thenReturn(dto);

                AuthInfoDTO result = authService.getMe(userId);

                assertSame(dto, result);
                assertEquals("mapped@mail.com", result.getEmail());
            }
        }
    }
}
