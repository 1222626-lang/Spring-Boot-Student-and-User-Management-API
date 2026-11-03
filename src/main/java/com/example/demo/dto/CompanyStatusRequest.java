package com.example.demo.dto;

import com.example.demo.entity.CompanyStatus;
import jakarta.validation.constraints.NotNull;

public record CompanyStatusRequest(
        @NotNull(message = "Company status is required")
        CompanyStatus status
) {
}