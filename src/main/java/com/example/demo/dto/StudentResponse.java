
package com.example.demo.dto;

public record StudentResponse(
        Long id,
        String firstName,
        String lastName,
        String email
) {
}
