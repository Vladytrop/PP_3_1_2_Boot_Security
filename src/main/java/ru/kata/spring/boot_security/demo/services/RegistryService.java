package ru.kata.spring.boot_security.demo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kata.spring.boot_security.demo.models.Role;
import ru.kata.spring.boot_security.demo.models.User;
import ru.kata.spring.boot_security.demo.repositories.RoleRepository;
import ru.kata.spring.boot_security.demo.repositories.URepository;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;


@Service
public class RegistryService {

    private final RoleRepository roleRepository;
    private final URepository uRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public RegistryService(RoleRepository roleRepository, URepository uRepository, PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.uRepository = uRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void register(User user) {

        if (uRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("Пользователь с таким логином уже существует");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        Role role = roleRepository.findFirstByName("ROLE_USER").orElseGet(
                () -> {
                    Role newRole = new Role();
                    newRole.setName("ROLE_USER");
                    roleRepository.save(newRole);
                    return newRole;
                }
        );
        user.setRoles(Set.of(role));
        uRepository.save(user);
//        Set<Role> role = roleRepository.findByName("ROLE_USER");
//
//        if (role.isEmpty()) {
//            Role useRole = new Role();
//            useRole.setName("ROLE_USER");
//            roleRepository.save(useRole);
//            user.setRoles(Set.of(useRole));
//        } else {
//            user.setRoles(role);
//        }
//        uRepository.save(user);
    }
}
