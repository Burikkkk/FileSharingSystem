package com.example.filesharing.Service;

import com.example.filesharing.Entities.User;
import com.example.filesharing.Repositories.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public boolean loginOrRegister(String username, String password, HttpServletRequest request) {

        Optional<User> existingUser = userRepository.findByUsername(username);

        if (existingUser.isEmpty()) {
            User newUser = User.builder()
                    .username(username)
                    .password(passwordEncoder.encode(password))
                    .build();
            userRepository.save(newUser);

            existingUser = userRepository.findByUsername(username);
            if (existingUser.isEmpty()) {
                throw new IllegalStateException("Не удалось создать пользователя");
            }
        }

        try {
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    username,
                    password
            );
            Authentication authentication = authenticationManager.authenticate(authToken);

            // Устанавливаем аутентификацию в контекст
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // ВАЖНО: сохраняем в сессии!
            HttpSession session = request.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    SecurityContextHolder.getContext());

            return true;

        } catch (AuthenticationException e) {
            return false;
        }
    }

}