
package com.example.demo.service;
import com.example.demo.dto.CompanyPatchRequest;
import com.example.demo.dto.CompanyRequest;
import com.example.demo.dto.CompanyResponse;

import com.example.demo.entity.Company;
import com.example.demo.entity.CompanyStatus;

import com.example.demo.exception.ResourceNotFoundException;

import com.example.demo.repository.CompanyRepository;
import com.example.demo.repository.UserRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@Transactional
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    public CompanyService(
            CompanyRepository companyRepository,
            UserRepository userRepository) {

        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
    }

    // ==========================================
    // NORMALIZE EMAIL
    // ==========================================

    private String normalizeEmail(String email) {

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Company email is required"
            );
        }

        return email.trim().toLowerCase(Locale.ROOT);
    }

    // ==========================================
    // GET COMPANY BY ID
    // ==========================================

    private Company getCompany(Long id) {

        return companyRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Company", id)
                );
    }

    // ==========================================
    // ENTITY TO RESPONSE DTO
    // ==========================================

    private CompanyResponse toResponse(Company company) {

        return new CompanyResponse(
                company.getId(),
                company.getName(),
                company.getEmail(),
                company.getAddress(),
                company.getStatus()
        );
    }

    // ==========================================
    // CREATE COMPANY
    // ==========================================

    public CompanyResponse create(CompanyRequest request) {

        String email = normalizeEmail(request.email());

        // Check duplicate email
        if (companyRepository.existsByEmailIgnoreCase(email)) {

            throw new IllegalArgumentException(
                    "Company email already exists"
            );
        }

        Company company = new Company();

        company.setName(request.name().trim());
        company.setEmail(email);

        company.setAddress(
                request.address() == null
                        ? null
                        : request.address().trim()
        );

        // New companies are ACTIVE by default
        company.setStatus(CompanyStatus.ACTIVE);

        Company savedCompany = companyRepository.save(company);

        return toResponse(savedCompany);
    }

    // ==========================================
    // GET ALL COMPANIES WITH PAGINATION
    // ==========================================

    @Transactional(readOnly = true)
    public Page<CompanyResponse> getAll(Pageable pageable) {

        return companyRepository.findAll(pageable)
                .map(this::toResponse);
    }

    // ==========================================
    // GET COMPANY BY ID
    // ==========================================

    @Transactional(readOnly = true)
    public CompanyResponse getById(Long id) {

        Company company = getCompany(id);

        return toResponse(company);
    }

    // ==========================================
    // UPDATE COMPANY - PUT
    // ==========================================

    public CompanyResponse update(
            Long id,
            CompanyRequest request) {

        Company company = getCompany(id);

        String email = normalizeEmail(request.email());

        // Check whether another company uses this email
        if (companyRepository.existsByEmailIgnoreCaseAndIdNot(
                email, id)) {

            throw new IllegalArgumentException(
                    "Company email already exists"
            );
        }

        company.setName(request.name().trim());
        company.setEmail(email);

        company.setAddress(
                request.address() == null
                        ? null
                        : request.address().trim()
        );

        Company updatedCompany = companyRepository.save(company);

        return toResponse(updatedCompany);
    }

    // ==========================================
    // PARTIAL UPDATE - PATCH
    // ==========================================

    public CompanyResponse patchCompany(
            Long id,
            CompanyPatchRequest request) {
        // 1. Find company
        Company company = getCompany(id);

        // 2. Update name only if provided
        if (request.getName() != null) {

            if (request.getName().isBlank()) {
                throw new IllegalArgumentException(
                        "Company name cannot be blank"
                );
            }

            company.setName(request.getName().trim());
        }

        // 3. Update email only if provided
        if (request.getEmail() != null) {

            String email = normalizeEmail(request.getEmail());

            // Check duplicate email
            if (companyRepository.existsByEmailIgnoreCaseAndIdNot(
                    email, id)) {

                throw new IllegalArgumentException(
                        "Company email already exists"
                );
            }

            company.setEmail(email);
        }

        // 4. Update address only if provided
        if (request.getAddress() != null) {
            company.setAddress(request.getAddress().trim());
        }

        // 5. Save updated company
        Company updatedCompany = companyRepository.save(company);

        // 6. Convert Entity to Response DTO
        return toResponse(updatedCompany);
    }

    // ==========================================
    // UPDATE COMPANY STATUS
    // ==========================================

    public CompanyResponse updateStatus(
            Long id,
            CompanyStatus status) {

        if (status == null) {

            throw new IllegalArgumentException(
                    "Company status is required"
            );
        }

        Company company = getCompany(id);

        company.setStatus(status);

        // When company is disabled:
        // Soft delete all active users belonging to this company.
        if (status == CompanyStatus.DISABLED) {

            userRepository.softDeleteAllByCompanyId(id);
        }

        Company updatedCompany = companyRepository.save(company);

        return toResponse(updatedCompany);
    }

    // ==========================================
    // DELETE COMPANY - SOFT DISABLE
    // ==========================================

    public void delete(Long id) {

        updateStatus(id, CompanyStatus.DISABLED);
    }
    public CompanyResponse restore(Long id) {

        Company company = getCompany(id);

        company.setStatus(CompanyStatus.ACTIVE);

        return toResponse(companyRepository.save(company));
    }
    public int restoreEmployees(Long companyId) {

        Company company = getCompany(companyId);

        if (company.getStatus() != CompanyStatus.ACTIVE) {
            throw new IllegalArgumentException(
                    "Cannot restore employees because the company is disabled"
            );
        }

        return userRepository.restoreAllByCompanyId(companyId);
    }
}
