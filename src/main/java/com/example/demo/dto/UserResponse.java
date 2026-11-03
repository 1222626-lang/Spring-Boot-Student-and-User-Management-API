package com.example.demo.dto;

import com.example.demo.entity.Role;

public record UserResponse(
        Long id,
        String username,
        String firstName,
        String lastName,
        String email,
        Role role,
        Long companyId,
        String companyName
) {
}
