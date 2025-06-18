package com.example.filesharing.Controllers;

import com.example.filesharing.Service.LoginService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;

    @GetMapping("/login")
    public String loginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {

        if (error != null) {
            model.addAttribute("error", "❌ Неверные учетные данные");
        }

        if (logout != null) {
            model.addAttribute("message", "✔️ Вы успешно вышли из системы");
        }

        return "login";
    }

    @PostMapping("/custom-login")
    public String processLogin(
            @RequestParam String username,
            @RequestParam String password,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        try {
            boolean authResult = loginService.loginOrRegister(username, password, request);

            if (!authResult) {
                redirectAttributes.addAttribute("error", true);
                return "redirect:/login";
            }

            return "redirect:/welcome";

        } catch (Exception e) {
            redirectAttributes.addAttribute("error", true);
            return "redirect:/login";
        }
    }

    @GetMapping("/welcome")
    public String welcomePage(Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/login";
        }

        model.addAttribute("username", principal.getName());
        return "welcome";
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request) throws ServletException {
        request.logout();
        return "redirect:/login?logout=true";
    }
}