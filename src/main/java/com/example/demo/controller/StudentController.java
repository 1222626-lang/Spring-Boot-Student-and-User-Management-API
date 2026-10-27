
package com.example.demo.controller;

import com.example.demo.dto.StudentRequest;
import com.example.demo.dto.StudentResponse;
import com.example.demo.service.StudentService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService service;

    public StudentController(StudentService service) {
        this.service = service;
    }

    @GetMapping
    public List<StudentResponse> getAllStudents() {
        return service.getAllStudents();
    }
    @GetMapping("/page")
    public Page<StudentResponse> getStudentsPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Page must be >= 0 and size must be between 1 and 100");
        }

        if (!Set.of("id", "firstName", "lastName", "email")
                .contains(sortBy)) {
            throw new IllegalArgumentException("Invalid sort field");
        }

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return service.getAll(pageable);
    }
    @PostMapping
    public StudentResponse createStudent(
            @Valid @RequestBody StudentRequest request) {

        return service.createStudent(request);
    }

    @PutMapping("/{id}")
    public StudentResponse updateStudent(
            @PathVariable Long id,
            @Valid @RequestBody StudentRequest request) {

        return service.updateStudent(id, request);
    }

    @PatchMapping("/{id}")
    public StudentResponse patchStudent(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates) {

        return service.patchStudent(id, updates);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(
            @PathVariable Long id) {

        service.deleteStudent(id);

        return ResponseEntity.noContent().build();
    }

    @RequestMapping(
            value = "/{id}",
            method = RequestMethod.HEAD)
    public ResponseEntity<Void> checkStudent(
            @PathVariable Long id) {

        return service.studentExists(id)
                ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }

    @RequestMapping(
            value = "/{id}",
            method = RequestMethod.OPTIONS)
    public ResponseEntity<Void> optionsStudent() {

        return ResponseEntity.ok()
                .headers(service.getAllowedMethods())
                .build();
    }
}
