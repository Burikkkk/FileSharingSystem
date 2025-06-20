package com.example.filesharing.Service;

import com.example.filesharing.Entities.User;
import com.example.filesharing.Repositories.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final UserRepository userRepository;

    public boolean loginOrRegister(String username, String password, HttpServletRequest request) {
        Optional<User> userOpt = userRepository.findByUsername(username);

        String hashedPassword = hashPassword(password);

        if (userOpt.isEmpty()) {
            // Регистрация нового пользователя
            User newUser = User.builder()
                    .username(username)
                    .password(hashedPassword)
                    .build();
            userRepository.save(newUser);
            userOpt = userRepository.findByUsername(username);
            if (userOpt.isEmpty()) {
                throw new IllegalStateException("Не удалось создать пользователя");
            }
        } else {
            // Проверка пароля
            User user = userOpt.get();
            if (!user.getPassword().equals(hashedPassword)) {
                return false; // Неверный пароль
            }
        }

        // Сохраняем логин и id пользователя в сессию
        User user = userOpt.get();
        HttpSession session = request.getSession(true);
        session.setAttribute("user", user);
        //session.setAttribute("username", user.getUsername());
        //session.setAttribute("user_id", user.getId());

        return true;
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.reset();
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Ошибка при хэшировании пароля", e);
        }
    }
}


