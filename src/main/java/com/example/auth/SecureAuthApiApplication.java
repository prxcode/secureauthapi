package com.example.auth;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.auth.model.User;
import com.example.auth.repository.UserRepository;

@SpringBootApplication
public class SecureAuthApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecureAuthApiApplication.class, args);
    }

    @Bean
    CommandLineRunner run(UserRepository userRepository, PasswordEncoder encoder) {
        return args -> {
            if (userRepository.findByUsername("testuser") == null) {
                User user = new User("testuser", encoder.encode("password123"), "ROLE_USER");
                userRepository.save(user);
            }
            if (userRepository.findByUsername("admin") == null) {
                User admin = new User("admin", encoder.encode("admin123"), "ROLE_ADMIN");
                userRepository.save(admin);
            }
        };
    }
}