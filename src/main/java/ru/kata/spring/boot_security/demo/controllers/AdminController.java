package ru.kata.spring.boot_security.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.models.Role;
import ru.kata.spring.boot_security.demo.models.User;
import ru.kata.spring.boot_security.demo.repositories.RoleRepository;
import ru.kata.spring.boot_security.demo.repositories.URepository;
import ru.kata.spring.boot_security.demo.services.RegistryService;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final URepository uRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private Role role;

    @Autowired
    public AdminController(URepository uRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.uRepository = uRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping()
    public String index(Model model) {
        model.addAttribute("users", uRepository.findAll());
        return "admin/index";
    }

    @GetMapping("/{id}")
    public String showUser(@PathVariable("id") int id, Model model) {
        model.addAttribute("user", uRepository.findById(id).orElse(null));
        return "admin/show";
    }

    @GetMapping("/new")
    public String newUser(@ModelAttribute("user") User user) {
        return "admin/new";
    }

    @PostMapping()
    public String createUser(@ModelAttribute("user") User user,
                             @RequestParam(value = "roleIds", required = false)Set<Long> roleIds,
                             BindingResult bindingResult) {
        if (bindingResult.hasErrors()){
            return "admin/new";
        }

        if(user.getUsername().isEmpty()){
            bindingResult.rejectValue("username"
                    , "username.empty"
                    ,"Имя не может быть пустым");
            return "admin/new";
        }

        if (user.getPassword().isEmpty()){
            bindingResult.rejectValue("password"
                    , "password.empty"
                    ,"Пароль не может быть пустым");
            return "admin/new";
        }

        user.setRoles(getRoles(roleIds));

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        uRepository.save(user);
        return "redirect:/admin";
    }

    @GetMapping("/{id}/edit")
    public String editUser(@PathVariable("id") int id, Model model) {
        model.addAttribute("user", uRepository.findById(id).orElse(null));
        return "admin/edit";
    }

    @PatchMapping("/{id}")
    public String updateUser(@ModelAttribute("user") User user,
                             @RequestParam(value = "roleIds", required = false)Set<Long> roleIds,
                             BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()){
            return "admin/edit";
        }

        User existUser = uRepository.findById(user.getId())
                        .orElseThrow(() ->
                                new RuntimeException("Пользователь не найден"));

        existUser.setUsername(user.getUsername());

        if(user.getUsername().isEmpty()){
            bindingResult.rejectValue("username"
                    , "username.empty"
                    ,"Имя не может быть пустым");
            return "admin/edit";
        }

        if (user.getPassword().isEmpty()){
            bindingResult.rejectValue("password"
                    ,"password.empty"
                    , "Пароль не может быть пустым");
            return "admin/edit";
        } else {
            existUser.setPassword(passwordEncoder.encode(user.getPassword()));
        }

       existUser.setRoles(getRoles(roleIds));

       uRepository.save(existUser);

       return "redirect:/admin";
    }

    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable("id") int id) {
        uRepository.deleteById(id);
        return "redirect:/admin";
    }

    public Set<Role> getRoles(Set<Long> roleIds){
        Set<Role> roles;
        if (roleIds == null || roleIds.isEmpty()){
            Role defRole = roleRepository.findFirstByName("ROLE_USER").orElseThrow(
                    () -> new RuntimeException("Роль не найдена")
            );
            roles = new HashSet<>();
            roles.add(defRole);
        } else {
            roles = new HashSet<>(roleRepository.findAllById(roleIds));
        }
        return roles;
    }
}
