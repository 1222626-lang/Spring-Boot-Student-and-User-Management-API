package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompanyPatchRequest(

        @Size(min = 1, max = 150)
        String name,

        @NotBlank(message = "Company email is required")
        @Email(message = "Invalid email format")
        String email,

        @Size(max = 255)
        String address

) {}