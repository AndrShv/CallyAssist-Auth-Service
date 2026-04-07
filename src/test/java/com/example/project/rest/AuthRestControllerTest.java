package com.example.project.rest;

import com.example.project.dto.AuthInfoDTO;
import com.example.project.dto.UserLoginDTO;
import com.example.project.dto.UserRegisterDTO;
import com.example.project.dto.UserResponseDTO;
import com.example.project.handlers.OAuth2SuccessHandler;
import com.example.project.interfaces.GenerateTokenForOAuth2;
import com.example.project.interfaces.GetMe;
import com.example.project.interfaces.Login;
import com.example.project.interfaces.Register;
import com.example.project.services.custom.CustomOAuth2UserService;
import com.example.project.services.custom.CustomUserDetailsService;
import com.example.project.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import java.util.UUID;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthRestController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
        "server.port=8081"
})
class AuthRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private Register register;


    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private CustomOAuth2UserService customOAuth2UserService;

    @MockBean
    private OAuth2SuccessHandler oAuth2SuccessHandler;

    @MockBean
    private Login login;

    @MockBean
    private GetMe getMe;

    @MockBean
    private GenerateTokenForOAuth2 generateTokenForOAuth2;

    @Nested
    @DisplayName("POST /api/auth/register")
    class RegisterEndpointTests {

        @Test
        @DisplayName("should register user successfully")
        void shouldRegisterUserSuccessfully() throws Exception {
            UserRegisterDTO request = new UserRegisterDTO();
            request.setEmail("test@mail.com");
            request.setUsername("Andrew");
            request.setPassword("password123");
            request.setRole("USER");

            UserResponseDTO response = UserResponseDTO.builder()
                    .id(UUID.randomUUID().toString())
                    .email("test@mail.com")
                    .username("Andrew")
                    .role("USER")
                    .token("jwt-token")
                    .build();

            when(register.register(any(UserRegisterDTO.class))).thenReturn(response);

            mockMvc.perform(post("/api/auth/register")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email", is("test@mail.com")))
                    .andExpect(jsonPath("$.username", is("Andrew")))
                    .andExpect(jsonPath("$.role", is("USER")))
                    .andExpect(jsonPath("$.token", is("jwt-token")));

            verify(register, times(1)).register(any(UserRegisterDTO.class));
        }

        @Test
        @DisplayName("should call register service with correct dto")
        void shouldCallRegisterServiceWithCorrectDto() throws Exception {
            UserRegisterDTO request = new UserRegisterDTO();
            request.setEmail("test@mail.com");
            request.setUsername("Andrew");
            request.setPassword("password123");
            request.setRole("USER");

            when(register.register(any(UserRegisterDTO.class)))
                    .thenReturn(UserResponseDTO.builder().build());

            mockMvc.perform(post("/api/auth/register")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            ArgumentCaptor<UserRegisterDTO> captor = ArgumentCaptor.forClass(UserRegisterDTO.class);
            verify(register).register(captor.capture());

            UserRegisterDTO captured = captor.getValue();
            assertEquals("test@mail.com", captured.getEmail());
            assertEquals("Andrew", captured.getUsername());
            assertEquals("password123", captured.getPassword());
            assertEquals("USER", captured.getRole());
        }

        @Test
        @DisplayName("should return correct response json for register")
        void shouldReturnCorrectRegisterJson() throws Exception {
            UserRegisterDTO request = new UserRegisterDTO();
            request.setEmail("test@mail.com");
            request.setUsername("Andrew");
            request.setPassword("password123");
            request.setRole("USER");

            UserResponseDTO response = UserResponseDTO.builder()
                    .id("123")
                    .email("test@mail.com")
                    .username("Andrew")
                    .role("USER")
                    .token("jwt-token")
                    .build();

            when(register.register(any(UserRegisterDTO.class))).thenReturn(response);

            mockMvc.perform(post("/api/auth/register")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is("123")))
                    .andExpect(jsonPath("$.email", is("test@mail.com")))
                    .andExpect(jsonPath("$.username", is("Andrew")))
                    .andExpect(jsonPath("$.role", is("USER")))
                    .andExpect(jsonPath("$.token", is("jwt-token")));
        }

        @Test
        @DisplayName("should return bad request when request body is invalid")
        void shouldReturnBadRequestWhenRegisterBodyInvalid() throws Exception {
            String invalidJson = """
                    {
                      "email": "",
                      "username": "",
                      "password": ""
                    }
                    """;

            mockMvc.perform(post("/api/auth/register")
                            .contentType(APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/auth/login")
    class LoginEndpointTests {

        @Test
        @DisplayName("should login successfully")
        void shouldLoginSuccessfully() throws Exception {
            UserLoginDTO request = new UserLoginDTO();
            request.setEmail("test@mail.com");
            request.setPassword("password123");

            UserResponseDTO response = UserResponseDTO.builder()
                    .id(UUID.randomUUID().toString())
                    .email("test@mail.com")
                    .username("Andrew")
                    .role("USER")
                    .token("jwt-token")
                    .build();

            when(login.login(any(UserLoginDTO.class))).thenReturn(response);

            mockMvc.perform(post("/api/auth/login")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email", is("test@mail.com")))
                    .andExpect(jsonPath("$.username", is("Andrew")))
                    .andExpect(jsonPath("$.role", is("USER")))
                    .andExpect(jsonPath("$.token", is("jwt-token")));

            verify(login, times(1)).login(any(UserLoginDTO.class));
        }

        @Test
        @DisplayName("should call login service with correct dto")
        void shouldCallLoginServiceWithCorrectDto() throws Exception {
            UserLoginDTO request = new UserLoginDTO();
            request.setEmail("test@mail.com");
            request.setPassword("password123");

            when(login.login(any(UserLoginDTO.class)))
                    .thenReturn(UserResponseDTO.builder().build());

            mockMvc.perform(post("/api/auth/login")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            ArgumentCaptor<UserLoginDTO> captor = ArgumentCaptor.forClass(UserLoginDTO.class);
            verify(login).login(captor.capture());

            UserLoginDTO captured = captor.getValue();
            assertEquals("test@mail.com", captured.getEmail());
            assertEquals("password123", captured.getPassword());
        }

        @Test
        @DisplayName("should return correct response json for login")
        void shouldReturnCorrectLoginJson() throws Exception {
            UserLoginDTO request = new UserLoginDTO();
            request.setEmail("test@mail.com");
            request.setPassword("password123");

            UserResponseDTO response = UserResponseDTO.builder()
                    .id("321")
                    .email("test@mail.com")
                    .username("Andrew")
                    .role("USER")
                    .token("jwt-token")
                    .build();

            when(login.login(any(UserLoginDTO.class))).thenReturn(response);

            mockMvc.perform(post("/api/auth/login")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is("321")))
                    .andExpect(jsonPath("$.email", is("test@mail.com")))
                    .andExpect(jsonPath("$.username", is("Andrew")))
                    .andExpect(jsonPath("$.role", is("USER")))
                    .andExpect(jsonPath("$.token", is("jwt-token")));
        }

        @Test
        @DisplayName("should return bad request when login body is invalid")
        void shouldReturnBadRequestWhenLoginBodyInvalid() throws Exception {
            String invalidJson = """
                    {
                      "email": "",
                      "password": ""
                    }
                    """;

            mockMvc.perform(post("/api/auth/login")
                            .contentType(APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/auth/me")
    class MeEndpointTests {

        @Test
        @DisplayName("should return auth info when authenticated")
        void shouldReturnAuthInfoWhenAuthenticated() throws Exception {
            UUID userId = UUID.randomUUID();

            AuthInfoDTO response = new AuthInfoDTO();
            response.setId(userId);
            response.setEmail("test@mail.com");
            response.setUsername("Andrew");

            when(getMe.getMe(userId)).thenReturn(response);

            mockMvc.perform(get("/api/auth/me")
                            .principal(authenticationPrincipal(userId, true)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(userId.toString())))
                    .andExpect(jsonPath("$.email", is("test@mail.com")))
                    .andExpect(jsonPath("$.username", is("Andrew")));

            verify(getMe).getMe(userId);
        }

        @Test
        @DisplayName("should return unauthorized when authentication is null")
        void shouldReturnUnauthorizedWhenAuthenticationIsNull() throws Exception {
            mockMvc.perform(get("/api/auth/me"))
                    .andExpect(status().isUnauthorized());

            verify(getMe, never()).getMe(any());
        }

        @Test
        @DisplayName("should return unauthorized when authentication is not authenticated")
        void shouldReturnUnauthorizedWhenAuthenticationIsNotAuthenticated() throws Exception {
            UUID userId = UUID.randomUUID();

            mockMvc.perform(get("/api/auth/me")
                            .principal(authenticationPrincipal(userId, false)))
                    .andExpect(status().isUnauthorized());

            verify(getMe, never()).getMe(any());
        }

        @Test
        @DisplayName("should call getMe service with correct user id")
        void shouldCallGetMeWithCorrectUserId() throws Exception {
            UUID userId = UUID.randomUUID();

            AuthInfoDTO response = new AuthInfoDTO();
            response.setId(userId);
            response.setEmail("test@mail.com");
            response.setUsername("Andrew");

            when(getMe.getMe(userId)).thenReturn(response);

            mockMvc.perform(get("/api/auth/me")
                            .principal(authenticationPrincipal(userId, true)))
                    .andExpect(status().isOk());

            verify(getMe, times(1)).getMe(userId);
        }
    }

    @Nested
    @DisplayName("GET /api/auth/oauth2/token")
    class OAuth2TokenEndpointTests {

        @Test
        @DisplayName("should generate oauth2 token successfully")
        void shouldGenerateOAuth2TokenSuccessfully() throws Exception {
            UUID userId = UUID.randomUUID();

            when(generateTokenForOAuth2.generateTokenForOAuth2(userId))
                    .thenReturn("oauth2-jwt-token");

            mockMvc.perform(get("/api/auth/oauth2/token")
                            .param("userId", userId.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token", is("oauth2-jwt-token")))
                    .andExpect(jsonPath("$.type", is("Bearer")));

            verify(generateTokenForOAuth2).generateTokenForOAuth2(userId);
        }

        @Test
        @DisplayName("should call oauth2 token service with correct user id")
        void shouldCallOAuth2TokenServiceWithCorrectUserId() throws Exception {
            UUID userId = UUID.randomUUID();

            when(generateTokenForOAuth2.generateTokenForOAuth2(userId))
                    .thenReturn("oauth2-jwt-token");

            mockMvc.perform(get("/api/auth/oauth2/token")
                            .param("userId", userId.toString()))
                    .andExpect(status().isOk());

            verify(generateTokenForOAuth2, times(1)).generateTokenForOAuth2(userId);
        }

        @Test
        @DisplayName("should return correct json for oauth2 token endpoint")
        void shouldReturnCorrectJsonForOAuth2Token() throws Exception {
            UUID userId = UUID.randomUUID();

            when(generateTokenForOAuth2.generateTokenForOAuth2(userId))
                    .thenReturn("my-special-token");

            mockMvc.perform(get("/api/auth/oauth2/token")
                            .param("userId", userId.toString()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                    .andExpect(jsonPath("$.token", is("my-special-token")))
                    .andExpect(jsonPath("$.type", is("Bearer")));
        }

        @Test
        @DisplayName("should return bad request for invalid uuid")
        void shouldReturnBadRequestForInvalidUuid() throws Exception {
            mockMvc.perform(get("/api/auth/oauth2/token")
                            .param("userId", "not-a-uuid"))
                    .andExpect(status().isBadRequest());

            verify(generateTokenForOAuth2, never()).generateTokenForOAuth2(any());
        }
    }

    private Authentication authenticationPrincipal(UUID userId, boolean authenticated) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userId);
        when(authentication.isAuthenticated()).thenReturn(authenticated);
        when(authentication.getName()).thenReturn(userId.toString());
        return authentication;
    }
}