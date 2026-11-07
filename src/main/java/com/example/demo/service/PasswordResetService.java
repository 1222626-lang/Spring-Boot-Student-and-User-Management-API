package com.example.demo.service;

import com.example.demo.dto.PasswordResetRequest;
import com.example.demo.dto.ResetPasswordRequest;

import com.example.demo.entity.PasswordResetToken;
import com.example.demo.entity.User;

import com.example.demo.repository.PasswordResetTokenRepository;
import com.example.demo.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PasswordResetService {

    private final UserRepository userRepository;

    private final PasswordResetTokenRepository tokenRepository;

    private final PasswordEncoder passwordEncoder;

    private final EmailService emailService;

    public PasswordResetService(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService) {

        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }
    @Transactional
    public String forgotPassword(
            PasswordResetRequest request) {

        // Normalize email
        String email = request.email()
                .trim()
                .toLowerCase();
        // Find active user
        User user = userRepository
                .findByEmailIgnoreCaseAndIsDeletedFalse(email)
                .orElse(null);

        // Do not reveal whether the account exists
        if (user == null) {

            return "If the account exists, reset instructions will be sent.";
        }

        // Delete previous token
        tokenRepository.deleteByUserId(user.getId());

        // Generate new token
        String token = UUID.randomUUID().toString();

        // Token expires after 15 minutes
        PasswordResetToken resetToken =
                new PasswordResetToken(
                        token,
                        user,
                        LocalDateTime.now().plusMinutes(15)
                );

        // Save token in database
        tokenRepository.saveAndFlush(resetToken);

        // Send email
        emailService.sendPasswordResetEmail(
                user.getEmail(),
                token
        );

        return "If the account exists, reset instructions will be sent.";
    }
    @Transactional
    public String resetPassword(
            ResetPasswordRequest request) {

        // Find token
        PasswordResetToken resetToken =
                tokenRepository.findByToken(
                                request.token().trim()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Invalid reset token"
                                )
                        );

        // Check whether token was used
        if (resetToken.isUsed()) {

            throw new IllegalArgumentException(
                    "Reset token has already been used"
            );
        }

        // Check token expiration
        if (!resetToken.getExpiryDate().isAfter(
                LocalDateTime.now())) {

            throw new IllegalArgumentException(
                    "Reset token has expired"
            );
        }

        // Get user
        User user = resetToken.getUser();

        // Check account is still active
        if (user.isDeleted() ||
                user.getCompany().getStatus() !=
                        com.example.demo.entity.CompanyStatus.ACTIVE) {

            throw new IllegalArgumentException(
                    "Account is not active"
            );
        }

        // Encode new password using BCrypt
        user.setPassword(
                passwordEncoder.encode(
                        request.newPassword()
                )
        );

        // Save updated user
        userRepository.save(user);

        // Mark token as used
        resetToken.setUsed(true);

        tokenRepository.save(resetToken);

        return "Password has been reset successfully";
    }
}