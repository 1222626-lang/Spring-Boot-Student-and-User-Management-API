package com.example.demo.dto;

import com.example.demo.entity.CompanyStatus;

public record CompanyResponse(
        Long id,
        String name,
        String email,
        String address,
        CompanyStatus status
) {
}


