package com.example.filesharing.ServiceTests;

import com.example.filesharing.Entities.User;
import com.example.filesharing.Repositories.UserRepository;
import com.example.filesharing.Service.LoginService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

class LoginServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    @InjectMocks
    private LoginService loginService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(request.getSession(true)).thenReturn(session);
    }

    @Test
    void testRegisterNewUser() {
        String username = "newuser";
        String password = "password";

        // Пользователь не найден
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // После сохранения пользователь найден
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        doAnswer(invocation -> {
            User userArg = invocation.getArgument(0);
            // Мокаем что пользователь теперь существует в репозитории
            when(userRepository.findByUsername(username)).thenReturn(Optional.of(userArg));
            return null;
        }).when(userRepository).save(userCaptor.capture());

        boolean result = loginService.loginOrRegister(username, password, request);

        assertTrue(result);

        User savedUser = userCaptor.getValue();
        assertEquals(username, savedUser.getUsername());
        assertNotEquals(password, savedUser.getPassword()); // должен быть захэширован

        verify(session).setAttribute("user", savedUser);
    }

    @Test
    void testCorrectPassword() {
        String username = "existing";
        String password = "mypassword";

        // Захэшируем пароль для сравнения
        String hashedPassword = loginService.hashPassword(password);

        User user = User.builder()
                .username(username)
                .password(hashedPassword)
                .build();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        boolean result = loginService.loginOrRegister(username, password, request);

        assertTrue(result);
        verify(session).setAttribute("user", user);
    }

    @Test
    void testWrongPassword() {
        String username = "existing";
        String password = "wrongpassword";

        User user = User.builder()
                .username(username)
                .password(loginService.hashPassword("correctpassword"))
                .build();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        boolean result = loginService.loginOrRegister(username, password, request);

        assertFalse(result);
        verify(session, never()).setAttribute(anyString(), any());
    }



}

