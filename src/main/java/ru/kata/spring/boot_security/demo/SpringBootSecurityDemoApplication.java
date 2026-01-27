package ru.kata.spring.boot_security.demo;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.kata.spring.boot_security.demo.models.Role;
import ru.kata.spring.boot_security.demo.models.User;
import ru.kata.spring.boot_security.demo.repositories.RoleRepository;
import ru.kata.spring.boot_security.demo.repositories.URepository;

import java.util.HashSet;
import java.util.Set;

@Configuration
@SpringBootApplication
public class SpringBootSecurityDemoApplication {


	private URepository uRepository;
	private RoleRepository roleRepository;

	@Autowired
    public SpringBootSecurityDemoApplication(URepository uRepository, RoleRepository roleRepository) {
        this.uRepository = uRepository;
        this.roleRepository = roleRepository;
    }

    public static void main(String[] args) {
		SpringApplication.run(SpringBootSecurityDemoApplication.class, args);
	}

	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}


	@Bean
	public CommandLineRunner userInitiator(PasswordEncoder passwordEncoder) {
		return args -> {
            if (uRepository.findByUsername("admin").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin"));

                Set<Role> rolesFind = roleRepository.findByName("ROLE_ADMIN");
				if(rolesFind.isEmpty()){
					throw new RuntimeException("Роль Админ не найдена");
				}

				Role adminRole = rolesFind.iterator().next();

				Set<Role> roles = new HashSet<>();
				roles.add(adminRole);
                admin.setRoles(roles);

                uRepository.save(admin);
                System.out.println("Администратор добавлен: \n логин: {admin} \n пароль: {admin}");
            } else {
				System.out.println("Администратор уже добавлен");
			}
        };
	}
}
