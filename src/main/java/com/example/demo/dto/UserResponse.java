
package com.example.demo.dto;

public record UserResponse(
        Long id,
        String username,
        String firstName,
        String lastName,
        String email
) {
}
