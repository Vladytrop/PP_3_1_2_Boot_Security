package ru.kata.spring.boot_security.demo.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.kata.spring.boot_security.demo.models.User;
import ru.kata.spring.boot_security.demo.repositories.URepository;
import ru.kata.spring.boot_security.demo.services.RegistryService;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final RegistryService registryService;
    private final URepository uRepository;

    public AuthController(RegistryService registryService, URepository uRepository) {
        this.registryService = registryService;
        this.uRepository = uRepository;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/registration")
    public String registrationPage(@ModelAttribute("user") User user) {
        return "auth/registration";
    }

    @PostMapping("/registration")
    public String registration(@ModelAttribute("user") User user, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "auth/registration";
        }

        if (uRepository.findByUsername(user.getUsername()).isPresent()){
            redirectAttributes.addAttribute("usernameError", "Имя пользователя уже занято");
            return "redirect:/auth/registration";
        }

        registryService.register(user);

        return "redirect:/auth/login";
    }
}
