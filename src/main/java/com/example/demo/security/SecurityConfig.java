
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

    private static final String SECRET =
            "demo-secret-key-for-jwt-learning-2026-very-long";

    // PASSWORD ENCODER
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // DATABASE USER DETAILS SERVICE
    @Bean
    public UserDetailsService userDetailsService(
            UserRepository userRepository) {

        return username -> {

            // Find active (not deleted) user and load company
            User user = userRepository
                    .findWithCompanyByUsernameIgnoreCaseAndIsDeletedFalse(username)
                    .orElseThrow(() ->
                            new UsernameNotFoundException("User not found"));

            // Check whether the user's company is active
            if (user.getCompany() == null ||
                    user.getCompany().getStatus() != CompanyStatus.ACTIVE) {

                throw new UsernameNotFoundException("Company is disabled");
            }

            // Return Spring Security UserDetails
            return org.springframework.security.core.userdetails.User
                    .withUsername(user.getUsername())
                    .password(user.getPassword())
                    .roles(user.getRole().name())
                    .build();
        };
    }

    // AUTHENTICATION MANAGER
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration)
            throws Exception {

        return configuration.getAuthenticationManager();
    }

    // JWT DECODER
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

        authoritiesConverter.setAuthoritiesClaimName("roles");
        authoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(
                authoritiesConverter
        );

        return converter;
    }

    // SECURITY FILTER CHAIN
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationConverter jwtConverter)
            throws Exception {

        http
                // Disable CSRF for stateless REST API
                .csrf(csrf -> csrf.disable())

                // JWT-based authentication: no HTTP session
                .sessionManagement(session -> session
                        .sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                // Authorization rules
                .authorizeHttpRequests(auth -> auth

                        // PUBLIC ENDPOINTS
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/forgot-password",
                                "/api/auth/reset-password",
                                "/api/public/**"
                        ).permitAll()

                        // PERSONAL USER ENDPOINTS
                        .requestMatchers(
                                "/api/users/me",
                                "/api/users/me/**"
                        ).hasAnyRole("USER", "ADMIN")

                        // ADMIN ONLY
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

                // Disable HTTP Basic authentication
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
