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

//    @GetMapping()
//    public String adminPage() {
//        return "admin/admin";
//    }

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

        if (user.getPassword().isEmpty()){
            bindingResult.rejectValue("password"
                    , "password.empty"
                    ,"Пароль не может быть пустым");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        Set<Role> roles;
        if (roleIds == null || roleIds.isEmpty()){
            Role defRole = roleRepository.findFirstByName("ROLE_USER").orElseThrow(
                    () -> new RuntimeException("Role not found")
            );
            roles = new HashSet<>();
            roles.add(defRole);
        } else {
            roles = new HashSet<>(roleRepository.findAllById(roleIds));
        }

        user.setRoles(roles);

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
                                new RuntimeException("User not found"));

        existUser.setUsername(user.getUsername());

        if (user.getPassword().isEmpty()){
            bindingResult.rejectValue("password"
                    ,"password.empty"
                    , "Пароль не может быть пустым");
            return "admin/edit";
        }
//        else {
//            user.setPassword(passwordEncoder.encode(user.getPassword()));
//        }

        if (!user.getPassword().isEmpty()){
           existUser.setPassword(passwordEncoder.encode(user.getPassword()));
        }

       Set<Role> roles;
       if (roleIds == null){
           roles = new HashSet<>();
           roles.add(roleRepository.findFirstByName("ROLE_USER").orElseThrow(
                   () -> new RuntimeException("Role not found")
           ));
       } else {
           roles = new HashSet<>(roleRepository.findAllById(roleIds));
       }

       existUser.setRoles(roles);

       uRepository.save(existUser);

       return "redirect:/admin";
    }

    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable("id") int id) {
        uRepository.deleteById(id);
        return "redirect:/admin";
    }
}
