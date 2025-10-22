package com.example.orzon_example.web;

import com.example.orzon_example.event.EventService;
import com.example.orzon_example.event.UserEventType;
import com.example.orzon_example.user.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;
    private final EventService eventService;

    public AuthController(UserService userService, EventService eventService) {
        this.userService = userService;
        this.eventService = eventService;
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("registerRequest", new UserService.RegisterRequest("", "", ""));
        return "auth/register";
    }

    @PostMapping("/auth/register")
    public String register(@Valid @ModelAttribute("registerRequest") UserService.RegisterRequest request,
                           BindingResult bindingResult,
                           RedirectAttributes attributes) {
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }
        try {
            var user = userService.registerUser(request);
            eventService.recordEvent(UserEventType.REGISTER, user, null, null);
            attributes.addFlashAttribute("message", "Registrierung erfolgreich. Bitte melden Sie sich an.");
            return "redirect:/login";
        } catch (IllegalArgumentException ex) {
            bindingResult.reject("registration", ex.getMessage());
            return "auth/register";
        }
    }

    @GetMapping("/login")
    public String loginForm() {
        return "auth/login";
    }

    @GetMapping("/auth/password-reset")
    public String passwordResetForm(Model model) {
        model.addAttribute("resetRequest", new PasswordResetForm());
        return "auth/password-reset";
    }

    @PostMapping("/auth/password-reset")
    public String passwordReset(@Valid @ModelAttribute("resetRequest") PasswordResetForm form,
                                BindingResult bindingResult,
                                RedirectAttributes attributes) {
        if (bindingResult.hasErrors()) {
            return "auth/password-reset";
        }
        try {
            userService.resetPassword(form.email(), form.newPassword());
            attributes.addFlashAttribute("message", "Passwort aktualisiert.");
            return "redirect:/login";
        } catch (IllegalArgumentException ex) {
            bindingResult.reject("reset", ex.getMessage());
            return "auth/password-reset";
        }
    }

    public record PasswordResetForm(@jakarta.validation.constraints.Email String email,
                                    @jakarta.validation.constraints.Size(min = 8) String newPassword) {
        public PasswordResetForm() {
            this("", "");
        }
    }
}
