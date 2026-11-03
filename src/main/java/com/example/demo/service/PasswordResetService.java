
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

    public PasswordResetService(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public String forgotPassword(PasswordResetRequest request) {

        String email = request.email()
                .trim()
                .toLowerCase();

        User user = userRepository
                .findByEmailIgnoreCaseAndIsDeletedFalse(email)
                .orElse(null);

        if (user == null) {
            return "If the account exists, reset instructions will be sent.";
        }

        // Delete previous reset token
        tokenRepository.deleteByUserId(user.getId());

        // Generate new reset token
        String token = UUID.randomUUID().toString();

        // Token expires after 15 minutes
        PasswordResetToken resetToken =
                new PasswordResetToken(
                        token,
                        user,
                        LocalDateTime.now().plusMinutes(15)
                );

        tokenRepository.saveAndFlush(resetToken);

        // Development testing only
        return "Reset token (development only): " + token;
    }

    @Transactional
    public String resetPassword(ResetPasswordRequest request) {

        // Find reset token
        PasswordResetToken resetToken =
                tokenRepository.findByToken(request.token().trim())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Invalid reset token"
                                ));

        // Check if token was already used
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

        // Encrypt new password using BCrypt
        user.setPassword(
                passwordEncoder.encode(request.newPassword())
        );

        userRepository.save(user);

        // Mark token as used
        resetToken.setUsed(true);

        tokenRepository.save(resetToken);

        return "Password has been reset successfully";
    }
}
