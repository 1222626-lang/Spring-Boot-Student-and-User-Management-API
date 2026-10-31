
package com.example.demo.service;

import com.example.demo.dto.StudentRequest;
import com.example.demo.dto.StudentResponse;
import com.example.demo.entity.Student;
import com.example.demo.exception.DuplicateEmailException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.StudentMapper;
import com.example.demo.repository.StudentRepository;
import com.example.demo.support.StudentSupportService;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class StudentService extends AbstractCrudService<
        Student, Long, StudentRequest, StudentResponse> {

    private final StudentRepository studentRepository;
    private final StudentSupportService supportService;

    public StudentService(
            StudentRepository studentRepository,
            StudentMapper studentMapper,
            StudentSupportService supportService) {

        super(studentRepository, studentMapper, "Student");

        this.studentRepository = studentRepository;
        this.supportService = supportService;
    }

    // NORMALIZE EMAIL
    private String normalizeEmail(String email) {

        if (email == null) {
            return null;
        }

        return email.trim().toLowerCase(Locale.ROOT);
    }

    // VALIDATE EMAIL FOR CREATE
    private void validateEmailForCreate(String email) {

        String normalizedEmail = normalizeEmail(email);

        if (studentRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new DuplicateEmailException(normalizedEmail);
        }
    }

    // VALIDATE EMAIL FOR UPDATE
    private void validateEmailForUpdate(
            String email, Long studentId) {

        String normalizedEmail = normalizeEmail(email);

        if (studentRepository.existsByEmailIgnoreCaseAndIdNot(
                normalizedEmail, studentId)) {

            throw new DuplicateEmailException(normalizedEmail);
        }
    }

    // CREATE STUDENT
    @Transactional
    public StudentResponse createStudent(StudentRequest request) {

        validateEmailForCreate(request.email());

        StudentRequest normalizedRequest = new StudentRequest(
                request.firstName(),
                request.lastName(),
                normalizeEmail(request.email())
        );

        return create(normalizedRequest);
    }

    // UPDATE STUDENT
    @Transactional
    public StudentResponse updateStudent(
            Long id, StudentRequest request) {

        validateEmailForUpdate(request.email(), id);

        StudentRequest normalizedRequest = new StudentRequest(
                request.firstName(),
                request.lastName(),
                normalizeEmail(request.email())
        );

        return update(id, normalizedRequest);
    }

    // PARTIAL UPDATE STUDENT
    @Transactional
    public StudentResponse patchStudent(
            Long id, Map<String, Object> updates) {

        supportService.validatePatchUpdates(updates);

        Student existing = studentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Student", id));

        for (Map.Entry<String, Object> entry : updates.entrySet()) {

            String value = (String) entry.getValue();

            switch (entry.getKey()) {

                case "firstName" ->
                        existing.setFirstName(value.trim());

                case "lastName" ->
                        existing.setLastName(value.trim());

                case "email" -> {

                    String normalizedEmail = normalizeEmail(value);

                    validateEmailForUpdate(normalizedEmail, id);

                    existing.setEmail(normalizedEmail);
                }

                default -> throw new IllegalArgumentException(
                        "Unknown field: " + entry.getKey());
            }
        }

        return mapper.toResponse(
                studentRepository.save(existing)
        );
    }

    // CHECK STUDENT EXISTS
    @Transactional(readOnly = true)
    public boolean studentExists(Long id) {

        return studentRepository.existsById(id);
    }

    // ALLOWED HTTP METHODS
    public HttpHeaders getAllowedMethods() {

        HttpHeaders headers = new HttpHeaders();

        headers.setAllow(Set.of(
                HttpMethod.GET,
                HttpMethod.HEAD,
                HttpMethod.POST,
                HttpMethod.PUT,
                HttpMethod.PATCH,
                HttpMethod.DELETE,
                HttpMethod.OPTIONS
        ));

        return headers;
    }
}
