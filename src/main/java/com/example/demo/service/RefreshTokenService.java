package com.example.demo.service;

import com.example.demo.entity.RefreshToken;
import com.example.demo.entity.User;
import com.example.demo.repository.RefreshTokenRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository repository;

    private final SecureRandom secureRandom = new SecureRandom();

    public RefreshTokenService(
            RefreshTokenRepository repository) {
        this.repository = repository;
    }

    // Generate random token
    private String generateToken() {

        byte[] bytes = new byte[64];
        secureRandom.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    // Hash token using SHA-256
    private String hashToken(String token) throws Exception {

        MessageDigest digest =
                MessageDigest.getInstance("SHA-256");

        byte[] hash = digest.digest(
                token.getBytes(StandardCharsets.UTF_8)
        );

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(hash);
    }

    // Create and save refresh token
    public String createRefreshToken(User user) {

        try {
            String rawToken = generateToken();

            RefreshToken refreshToken = new RefreshToken(
                    hashToken(rawToken),
                    user,
                    LocalDateTime.now().plusDays(7)
            );

            repository.save(refreshToken);

            return rawToken;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Verify token
    @Transactional
    public User verifyToken(String rawToken) {

        try {
            String hash = hashToken(rawToken);

            RefreshToken token = repository
                    .findByTokenHash(hash)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Invalid refresh token"));
//token is remove OR after exipry date
            if (token.isRevoked() ||
                    !token.getExpiryDate().isAfter(
                            LocalDateTime.now())) {

                throw new IllegalArgumentException(
                        "Refresh token expired or revoked");
            }

            User user = token.getUser();
// search user is delete ? or company for user is not active ?
            if (user.isDeleted() ||
                    user.getCompany().getStatus()
                            != com.example.demo.entity.CompanyStatus.ACTIVE) {
                throw new IllegalArgumentException(
                        "Account is not active");
            }

            return user;

        }
        catch (IllegalArgumentException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Revoke token
    @Transactional
    public void revokeToken(String rawToken) {

        try {
            String hash = hashToken(rawToken);
//confort status revoked from false to true
            repository.findByTokenHash(hash)
                    .ifPresent(token -> token.setRevoked(true));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}