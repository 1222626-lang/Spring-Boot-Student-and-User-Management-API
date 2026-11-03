package com.example.demo.controller;

import com.example.demo.dto.PasswordResetRequest;
import com.example.demo.dto.ResetPasswordRequest;
import com.example.demo.service.PasswordResetService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class PasswordResetController {

    private final PasswordResetService service;

    public PasswordResetController(
            PasswordResetService service) {

        this.service = service;
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @Valid @RequestBody PasswordResetRequest request) {

        String result = service.forgotPassword(request);

        return ResponseEntity.ok(
                Map.of("message", result));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        String result = service.resetPassword(request);

        return ResponseEntity.ok(
                Map.of("message", result));
    }
}