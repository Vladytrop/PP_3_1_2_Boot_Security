package ru.kata.spring.boot_security.demo.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.models.Role;
import ru.kata.spring.boot_security.demo.models.User;
import ru.kata.spring.boot_security.demo.repositories.RoleRepository;
import ru.kata.spring.boot_security.demo.repositories.URepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRestController {

    private final URepository uRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;

    public AdminRestController(URepository uRepository, RoleRepository roleRepository, PasswordEncoder encoder) {
        this.uRepository = uRepository;
        this.roleRepository = roleRepository;
        this.encoder = encoder;
    }

    @GetMapping("/users")
    public List<User> getUsers() {
        return uRepository.findAll();
    }

    @GetMapping("/users/{id}")
    public User getUser(@PathVariable int id) {
        return uRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Пользователь не найден")
        );
    }

    @PostMapping("/users")
    public User createUser(@RequestBody User user,
                           @RequestParam(required = false) Set<Long> roleIds) {

        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(encoder.encode(user.getPassword()));
        }

        user.setRoles(getRoles(roleIds));
        return uRepository.save(user);
    }

    @PatchMapping("/users/{id}")
    public User updateUser(@PathVariable int id,
                           @RequestBody User user,
                           @RequestParam(required = false) Set<Long> roleIds) {
        User exist = uRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Пользователь не найден")
        );

        exist.setUsername(user.getUsername());
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            exist.setPassword(encoder.encode(user.getPassword()));
        }

        exist.setRoles(getRoles(roleIds));
        return uRepository.save(exist);
    }


    @DeleteMapping("/users/{id}")
    public void deleteUser(@PathVariable int id) {
        uRepository.deleteById(id);
    }

    private Set<Role> getRoles(Set<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            Role def = roleRepository.findFirstByName("ROLE_USER").orElseThrow();
            return Set.of(def);
        }
        return new HashSet<>(roleRepository.findAllById(roleIds));
    }
}
