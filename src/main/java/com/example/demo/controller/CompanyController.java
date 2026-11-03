
package com.example.demo.controller;

import com.example.demo.dto.CompanyPatchRequest;
import com.example.demo.dto.CompanyRequest;
import com.example.demo.dto.CompanyResponse;
import com.example.demo.dto.CompanyStatusRequest;

import com.example.demo.service.CompanyService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    private final CompanyService service;

    public CompanyController(CompanyService service) {
        this.service = service;
    }

    // ==========================================
    // CREATE COMPANY
    // POST /api/companies
    // ==========================================

    @PostMapping
    public ResponseEntity<CompanyResponse> create(
            @Valid @RequestBody CompanyRequest request) {

        CompanyResponse response = service.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // ==========================================
    // GET ALL COMPANIES WITH PAGINATION
    // GET /api/companies
    // GET /api/companies/page
    // ==========================================

    @GetMapping({"", "/page"})
    public Page<CompanyResponse> getAll(Pageable pageable) {

        return service.getAll(pageable);
    }

    // ==========================================
    // GET COMPANY BY ID
    // GET /api/companies/{id}
    // ==========================================

    @GetMapping("/{id}")
    public CompanyResponse getById(
            @PathVariable Long id) {

        return service.getById(id);
    }

    // ==========================================
    // UPDATE COMPANY - PUT
    // PUT /api/companies/{id}
    // ==========================================

    @PutMapping("/{id}")
    public CompanyResponse update(
            @PathVariable Long id,
            @Valid @RequestBody CompanyRequest request) {

        return service.update(id, request);
    }

    // ==========================================
    // PARTIAL UPDATE - PATCH
    // PATCH /api/companies/{id}
    // ==========================================

    @PatchMapping("/{id}")
    public CompanyResponse patch(
            @PathVariable Long id,
            @Valid @RequestBody CompanyPatchRequest request) {

        return service.patch(id, request);
    }

    // ==========================================
    // UPDATE COMPANY STATUS
    // PATCH /api/companies/{id}/status
    // ==========================================

    @PatchMapping("/{id}/status")
    public CompanyResponse updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody CompanyStatusRequest request) {

        return service.updateStatus(
                id,
                request.status()
        );
    }

    // ==========================================
    // DELETE COMPANY - SOFT DISABLE
    // DELETE /api/companies/{id}
    // ==========================================

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id) {

        service.delete(id);

        return ResponseEntity.noContent().build();
    }

    // ==========================================
    // HEAD REQUEST
    // HEAD /api/companies/{id}
    // ==========================================

    @RequestMapping(
            value = "/{id}",
            method = RequestMethod.HEAD
    )
    public ResponseEntity<Void> head(
            @PathVariable Long id) {

        service.getById(id);

        return ResponseEntity.ok().build();
    }

    // ==========================================
    // OPTIONS REQUEST
    // OPTIONS /api/companies
    // OPTIONS /api/companies/{id}
    // ==========================================

    @RequestMapping(
            value = {"", "/{id}"},
            method = RequestMethod.OPTIONS
    )
    public ResponseEntity<Void> options() {

        HttpHeaders headers = new HttpHeaders();

        headers.setAllow(Set.of(
                HttpMethod.GET,
                HttpMethod.POST,
                HttpMethod.PUT,
                HttpMethod.PATCH,
                HttpMethod.DELETE,
                HttpMethod.HEAD,
                HttpMethod.OPTIONS
        ));

        return ResponseEntity
                .ok()
                .headers(headers)
                .build();
    }
    @PatchMapping("/{id}/restore")
    public CompanyResponse restore(@PathVariable Long id) {
        return service.restore(id);
    }
    @PostMapping("/{id}/restore-users")
    public ResponseEntity<Integer> restoreEmployees(
            @PathVariable Long id) {

        int restoredCount = service.restoreEmployees(id);

        return ResponseEntity.ok(restoredCount);
    }
}
