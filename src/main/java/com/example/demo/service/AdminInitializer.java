
package com.example.demo.service;

import com.example.demo.entity.Company;
import com.example.demo.entity.CompanyStatus;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;

import com.example.demo.repository.CompanyRepository;
import com.example.demo.repository.UserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username:}")
    private String username;

    @Value("${app.admin.password:}")
    private String password;

    public AdminInitializer(
            UserRepository userRepository,
            CompanyRepository companyRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {

        // Validate admin configuration
        if (username == null || username.isBlank()
                || password == null || password.length() < 12) {

            System.out.println(
                    "Admin bootstrap skipped: configure admin credentials."
            );

            return;
        }

        // Avoid creating duplicate admin accounts
        if (userRepository.existsByUsernameIgnoreCase(username.trim())) {

            System.out.println("Admin account already exists.");

            return;
        }

        // System administration company
        String systemEmail = "system-admin-company@example.invalid";

        Company company = companyRepository
                .findByEmailIgnoreCase(systemEmail)
                .orElseGet(() -> {

                    Company newCompany = new Company(
                            "System Administration",
                            systemEmail,
                            "System"
                    );

                    newCompany.setStatus(CompanyStatus.ACTIVE);

                    return companyRepository.save(newCompany);
                });

        // Ensure the system company is active
        if (company.getStatus() != CompanyStatus.ACTIVE) {
            company.setStatus(CompanyStatus.ACTIVE);
            companyRepository.save(company);
        }

        // Create initial admin
        User admin = new User();

        admin.setUsername(username.trim().toLowerCase());
        admin.setFirstName("System");
        admin.setLastName("Administrator");
        admin.setEmail("system-admin@example.invalid");

        // Encrypt password using BCrypt
        admin.setPassword(passwordEncoder.encode(password));

        admin.setRole(Role.ADMIN);
        admin.setCompany(company);

        userRepository.save(admin);

        System.out.println("Initial ADMIN account created successfully.");
    }
}
