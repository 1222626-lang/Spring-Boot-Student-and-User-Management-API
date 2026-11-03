
package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CompanyRequest(

        @NotBlank(message = "Company name is required")
        String name,

        @NotBlank(message = "Company email is required")
        @Email(message = "Invalid email format")
        String email,

        String address

) {
}
