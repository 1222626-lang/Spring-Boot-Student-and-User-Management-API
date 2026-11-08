package com.example.demo.security;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.RefreshTokenService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;

    public AuthController(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            UserRepository userRepository,
            RefreshTokenService refreshTokenService) {

        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.refreshTokenService = refreshTokenService;
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request) {

        // 1. Authenticate username and password
        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.username(),
                                request.password()
                        )
                );

        // 2. Get user from database
        User user = userRepository
                .findWithCompanyByUsernameIgnoreCaseAndIsDeletedFalse(
                        authentication.getName()
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "User not found"
                        )
                );

        // 3. Generate Access Token with user information
        String accessToken = jwtService.generateToken(user);

        // 4. Generate Refresh Token and store its hash in database
        String refreshToken =refreshTokenService.createRefreshToken(user);

        // 5. Return both tokens
        return ResponseEntity.ok(
                Map.of(
                        "accessToken", accessToken,
                        "refreshToken", refreshToken,
                        "tokenType", "Bearer",
                        "username", user.getUsername(),
                        "role", user.getRole().name()
                )
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(
            @RequestBody RefreshRequest request) {

        // 1. Validate the old Refresh Token
        User user = refreshTokenService.verifyToken(request.refreshToken());

        // 2. Revoke the old Refresh Token
        refreshTokenService.revokeToken(request.refreshToken());

        // 3. Generate a new Access Token
        String newAccessToken = jwtService.generateToken(user);

        // 4. Generate a new Refresh Token
        String newRefreshToken =refreshTokenService.createRefreshToken(user);

        // 5. Return the new tokens
        return ResponseEntity.ok(
                Map.of(
                        "accessToken", newAccessToken,
                        "refreshToken", newRefreshToken,
                        "tokenType", "Bearer"
                )
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @RequestBody LogoutRequest request) {

        // Revoke the Refresh Token in database
        refreshTokenService.revokeToken(
                request.refreshToken()
        );

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Logged out successfully"
                )
        );
    }

    public record LoginRequest(
            String username,
            String password
    ) {
    }

    public record RefreshRequest(
            String refreshToken
    ) {
    }

    public record LogoutRequest(
            String refreshToken
    ) {
    }
}