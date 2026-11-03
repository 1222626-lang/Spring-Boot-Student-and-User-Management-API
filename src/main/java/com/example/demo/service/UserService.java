package com.example.demo.service;

import com.example.demo.dto.UserRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.entity.Company;
import com.example.demo.entity.CompanyStatus;
import com.example.demo.entity.User;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.UserMapper;
import com.example.demo.repository.CompanyRepository;
import com.example.demo.repository.UserRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       UserMapper userMapper,
                       CompanyRepository companyRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.companyRepository = companyRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().toLowerCase(Locale.ROOT);
    }

    private void validateUnique(String username, String email, Long currentId) {
        String normalizedUsername = normalize(username);
        String normalizedEmail = normalize(email);

        boolean usernameExists = currentId == null
                ? userRepository.existsByUsernameIgnoreCase(normalizedUsername)
                : userRepository.existsByUsernameIgnoreCaseAndIdNot(
                normalizedUsername, currentId);

        boolean emailExists = currentId == null
                ? userRepository.existsByEmailIgnoreCase(normalizedEmail)
                : userRepository.existsByEmailIgnoreCaseAndIdNot(
                normalizedEmail, currentId);

        if (usernameExists) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (emailExists) {
            throw new IllegalArgumentException("Email already exists");
        }
    }

    private Company getActiveCompany(Long companyId) {
        if (companyId == null) {
            throw new IllegalArgumentException("Company ID is required");
        }

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", companyId));

        if (company.getStatus() != CompanyStatus.ACTIVE) {
            throw new IllegalArgumentException("Cannot assign user to a disabled company");
        }
        return company;
    }

    private User getActiveUser(Long id) {
        return userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> getAll(Pageable pageable) {
        return userRepository.findAllByIsDeletedFalse(pageable)
                .map(userMapper::toResponse);
    }

    public UserResponse create(UserRequest request) {
        validateUnique(request.username(), request.email(), null);

        if (request.password() == null || request.password().isBlank()
                || request.password().length() < 8) {
            throw new IllegalArgumentException(
                    "Password must contain at least 8 characters");
        }

        Company company = getActiveCompany(request.companyId());

        User user = new User();
        user.setUsername(normalize(request.username()));
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setEmail(normalize(request.email()));
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        user.setCompany(company);

        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        return userMapper.toResponse(getActiveUser(id));
    }

    public UserResponse update(Long id, UserRequest request) {
        User user = getActiveUser(id);
        validateUnique(request.username(), request.email(), id);

        user.setUsername(normalize(request.username()));
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setEmail(normalize(request.email()));
        user.setRole(request.role());
        user.setCompany(getActiveCompany(request.companyId()));

        if (request.password() != null && !request.password().isBlank()) {
            if (request.password().length() < 8) {
                throw new IllegalArgumentException(
                        "Password must contain at least 8 characters");
            }
            user.setPassword(passwordEncoder.encode(request.password()));
        }

        return userMapper.toResponse(userRepository.save(user));
    }

    public void delete(Long id) {
        User user = getActiveUser(id);
        user.softDelete();
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> getDeletedUsers(Pageable pageable) {
        return userRepository.findAllByIsDeletedTrue(pageable)
                .map(userMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public UserResponse getDeletedUserById(Long id) {
        User user = userRepository.findByIdAndIsDeletedTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Deleted user", id));
        return userMapper.toResponse(user);
    }

    public UserResponse restore(Long id) {
        User user = userRepository.findByIdAndIsDeletedTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Deleted user", id));

        if (user.getCompany().getStatus() != CompanyStatus.ACTIVE) {
            throw new IllegalArgumentException(
                    "Cannot restore user because the company is disabled");
        }

        user.restore();
        return userMapper.toResponse(userRepository.save(user));
    }
}
