package com.example.demo.security;

import com.example.demo.entity.CompanyStatus;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    // Must be identical to the secret used in JwtService
    private static final String SECRET =
            "demo-secret-key-for-jwt-learning-2026-very-long";

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(
            UserRepository userRepository) {

        return username -> {

            // Find user who is not deleted
            User user = userRepository
                    .findWithCompanyByUsernameIgnoreCaseAndIsDeletedFalse(
                            username
                    )
                    .orElseThrow(() ->
                            new UsernameNotFoundException(
                                    "User not found"
                            )
                    );

            // Check if company exists and is active
            if (user.getCompany() == null ||
                    user.getCompany().getStatus()
                            != CompanyStatus.ACTIVE) {

                throw new UsernameNotFoundException(
                        "Company is disabled"
                );
            }

            // Return Spring Security UserDetails
            return org.springframework.security.core.userdetails.User
                    .withUsername(user.getUsername())
                    .password(user.getPassword())
                    .roles(user.getRole().name())
                    .build();
        };
    }


    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration)
            throws Exception {

        return configuration.getAuthenticationManager();
    }


    @Bean
    public JwtDecoder jwtDecoder() {

        SecretKey key = new SecretKeySpec(
                SECRET.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );

        return NimbusJwtDecoder.withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    // JWT AUTHORITY CONVERTER

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtGrantedAuthoritiesConverter authoritiesConverter =
                new JwtGrantedAuthoritiesConverter();

        // Read roles from JWT claim named "roles"
        authoritiesConverter.setAuthoritiesClaimName("roles");

        // Convert USER to ROLE_USER and ADMIN to ROLE_ADMIN
        authoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(
                authoritiesConverter
        );

        return converter;
    }


    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationConverter jwtConverter)
            throws Exception {

        http

                // Disable CSRF for stateless REST API
                .csrf(csrf -> csrf.disable())

                // No HTTP session
                .sessionManagement(session -> session
                        .sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                // =========================
                // AUTHORIZATION RULES
                // =========================

                .authorizeHttpRequests(auth -> auth

                        // PUBLIC ENDPOINTS
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/refresh",
                                "/api/auth/logout",
                                "/api/auth/forgot-password",
                                "/api/auth/reset-password",
                                "/api/public/**"
                        ).permitAll()

                        // PERSONAL USER ENDPOINTS
                        // USER and ADMIN can access their own profile
                        .requestMatchers(
                                "/api/users/me",
                                "/api/users/me/**"
                        ).hasAnyRole("USER", "ADMIN")

                        // ADMIN ONLY ENDPOINTS
                        .requestMatchers(
                                "/api/admin/**",
                                "/api/users/**",
                                "/api/companies/**"
                        ).hasRole("ADMIN")

                        // ALL OTHER ENDPOINTS
                        .anyRequest().authenticated()
                )

                // Disable form login
                .formLogin(form -> form.disable())

                // Disable HTTP Basic
                .httpBasic(basic -> basic.disable())

                // Enable JWT authentication
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtConverter)
                        )
                );

        return http.build();
    }
}