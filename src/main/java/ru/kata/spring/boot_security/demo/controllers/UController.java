package ru.kata.spring.boot_security.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.kata.spring.boot_security.demo.models.User;
import ru.kata.spring.boot_security.demo.repositories.URepository;

import java.security.Principal;

@Controller
@RequestMapping("/user")
public class UController {

    private final URepository uRepository;

    @Autowired
    public UController(URepository uRepository) {
        this.uRepository = uRepository;
    }

    @GetMapping()
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public String userPage(Model model, Principal principal) {
        User user = uRepository.findByUsername(principal.getName()).orElseThrow(
                () -> new RuntimeException("Пользователь не найден"));
        model.addAttribute("user", user);
        return "user-page";
    }
}
