
package com.example.demo.controller;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.dto.UserProfileUpdateRequest;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users/me")
public class UserProfileController {

    private final UserRepository userRepository;

    public UserProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getMyProfile(
            Authentication authentication) {

        User user = getCurrentUser(authentication);

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("id", user.getId());
        response.put("username", user.getUsername());
        response.put("firstName", user.getFirstName());
        response.put("lastName", user.getLastName());
        response.put("email", user.getEmail());
        response.put("role", user.getRole());

        response.put(
                "companyId",
                user.getCompany() != null
                        ? user.getCompany().getId()
                        : null
        );

        response.put(
                "companyName",
                user.getCompany() != null
                        ? user.getCompany().getName()
                        : null
        );

        return ResponseEntity.ok(response);
    }

    // GET CURRENT USER COMPANY
    @GetMapping("/company")
    public ResponseEntity<Map<String, Object>> getMyCompany(
            Authentication authentication) {

        User user = getCurrentUser(authentication);

        if (user.getCompany() == null) {
            return ResponseEntity.noContent().build();
        }

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("companyId", user.getCompany().getId());
        response.put("companyName", user.getCompany().getName());

        return ResponseEntity.ok(response);
    }


    // UPDATE CURRENT USER PROFILE
    @PutMapping
    public ResponseEntity<Map<String, Object>> updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody UserProfileUpdateRequest request) {

        User user = getCurrentUser(authentication);

        String email = request.email().trim().toLowerCase();

        if (userRepository.existsByEmailIgnoreCaseAndIdNot(
                email, user.getId())) {

            throw new IllegalArgumentException(
                    "Email already exists");
        }

        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setEmail(email);

        userRepository.save(user);

        return getMyProfile(authentication);
    }

    // GET LOGGED-IN USER FROM JWT
    private User getCurrentUser(Authentication authentication) {


        return userRepository
                .findWithCompanyByUsernameIgnoreCaseAndIsDeletedFalse(
                        authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

    }
}
