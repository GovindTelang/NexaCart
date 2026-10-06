package com.govind.ecommerce.service;

import com.govind.ecommerce.model.Role;
import com.govind.ecommerce.model.User;
import com.govind.ecommerce.repo.UserRepository;
import com.govind.ecommerce.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(
                userRepository,
                passwordEncoder,
                jwtService
        );
    }

    @Test
    void registerUser_shouldEncodePasswordAndSaveUser() {

        User user = new User();
        user.setName("Test User");
        user.setEmail("test@nexacart.local");
        user.setPassword("Test@1234");

        when(passwordEncoder.encode("Test@1234"))
                .thenReturn("hashed-password");

        when(userRepository.save(user))
                .thenReturn(user);

        User result = userService.registerUser(user);

        assertEquals("hashed-password", result.getPassword());

        verify(passwordEncoder).encode("Test@1234");
        verify(userRepository).save(user);
    }

    @Test
    void loginUser_shouldReturnJwtForValidCredentials() {

        User user = new User();
        user.setEmail("test@nexacart.local");
        user.setPassword("hashed-password");
        user.setRole(Role.USER);

        when(userRepository.findByEmail("test@nexacart.local"))
                .thenReturn(user);

        when(passwordEncoder.matches(
                "Test@1234",
                "hashed-password"
        )).thenReturn(true);

        when(jwtService.generateToken(
                "test@nexacart.local",
                "USER"
        )).thenReturn("test-jwt-token");

        String token = userService.loginUser(
                "test@nexacart.local",
                "Test@1234"
        );

        assertEquals("test-jwt-token", token);

        verify(jwtService).generateToken(
                "test@nexacart.local",
                "USER"
        );
    }

    @Test
    void loginUser_shouldReturnNullForInvalidPassword() {

        User user = new User();
        user.setEmail("test@nexacart.local");
        user.setPassword("hashed-password");
        user.setRole(Role.USER);

        when(userRepository.findByEmail("test@nexacart.local"))
                .thenReturn(user);

        when(passwordEncoder.matches(
                "WrongPassword",
                "hashed-password"
        )).thenReturn(false);

        String token = userService.loginUser(
                "test@nexacart.local",
                "WrongPassword"
        );

        assertNull(token);

        verify(jwtService, never()).generateToken(
                anyString(),
                anyString()
        );
    }
}
