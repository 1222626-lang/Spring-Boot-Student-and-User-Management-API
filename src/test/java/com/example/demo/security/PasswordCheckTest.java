package com.example.demo.security;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
class PasswordCheckTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void checkOmarPassword() {

        User user = userRepository.findByUsernameIgnoreCaseAndIsDeletedFalse("omar1")
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        System.out.println("Username: " + user.getUsername());
        System.out.println("Role: " + user.getRole());

        boolean matches = passwordEncoder.matches(
                "Student123!",
                user.getPassword()
        );

        System.out.println("Password matches: " + matches);
    }
}