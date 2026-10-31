
package com.example.demo.controller;

import com.example.demo.dto.StudentRequest;
import com.example.demo.dto.StudentResponse;
import com.example.demo.service.StudentService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService service;

    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of("id", "firstName", "lastName", "email");

    public StudentController(StudentService service) {
        this.service = service;
    }

    // GET ALL WITH PAGINATION
    @GetMapping({"", "/page"})
    public Page<StudentResponse> getAllStudents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        validatePagination(page, size);
        validateSorting(sortBy, direction);

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return service.getAll(pageable);
    }

    // CREATE
    @PostMapping
    public StudentResponse createStudent(
            @Valid @RequestBody StudentRequest request) {

        return service.createStudent(request);
    }

    // UPDATE
    @PutMapping("/{id}")
    public StudentResponse updateStudent(
            @PathVariable Long id,
            @Valid @RequestBody StudentRequest request) {

        return service.updateStudent(id, request);
    }

    // PARTIAL UPDATE
    @PatchMapping("/{id}")
    public StudentResponse patchStudent(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates) {

        return service.patchStudent(id, updates);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(
            @PathVariable Long id) {

        service.delete(id);

        return ResponseEntity.noContent().build();
    }

    // HEAD
    @RequestMapping(
            value = "/{id}",
            method = RequestMethod.HEAD)
    public ResponseEntity<Void> checkStudent(
            @PathVariable Long id) {

        return service.studentExists(id)
                ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }

    // OPTIONS
    @RequestMapping(
            value = "/{id}",
            method = RequestMethod.OPTIONS)
    public ResponseEntity<Void> optionsStudent() {

        HttpHeaders headers = service.getAllowedMethods();

        return ResponseEntity.ok()
                .headers(headers)
                .build();
    }

    // VALIDATE PAGINATION
    private void validatePagination(int page, int size) {

        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page number must be >= 0");
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and 100");
        }
    }

    // VALIDATE SORTING
    private void validateSorting(String sortBy, String direction) {

        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new IllegalArgumentException(
                    "Invalid sort field: " + sortBy);
        }

        if (!direction.equalsIgnoreCase("asc")
                && !direction.equalsIgnoreCase("desc")) {

            throw new IllegalArgumentException(
                    "Direction must be either asc or desc");
        }
    }
}
