package ru.kata.spring.boot_security.demo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kata.spring.boot_security.demo.repositories.URepository;

@Service
public class UDetailsService implements UserDetailsService {

    private final URepository uRepository;

    @Autowired
    public UDetailsService(URepository uRepository) {
        this.uRepository = uRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return uRepository.findByUsername(username).orElseThrow(
                () -> new UsernameNotFoundException("Пользователь не найден"));
    }
}
