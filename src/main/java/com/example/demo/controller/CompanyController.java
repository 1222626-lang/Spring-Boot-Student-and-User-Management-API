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
    public CompanyController(CompanyService service)
    {
        this.service = service;
    }
    @PostMapping
    public ResponseEntity<CompanyResponse> create(
            @Valid @RequestBody CompanyRequest request) {
        CompanyResponse response = service.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
    @GetMapping({"", "/page"})
    public Page<CompanyResponse> getAll(Pageable pageable) {
        return service.getAll(pageable);
    }
    @GetMapping("/{id}")
    public CompanyResponse getById(
            @PathVariable Long id) {

        return service.getById(id);
    }
    @PutMapping("/{id}")
    public CompanyResponse update(
            @PathVariable Long id,
            @Valid @RequestBody CompanyRequest request) {
        return service.update(id, request);
    }
    @PatchMapping("/{id}")
    public CompanyResponse patch(
            @PathVariable Long id,
            @Valid @RequestBody CompanyPatchRequest request) {
        return service.patchCompany(id, request);
    }
    @PatchMapping("/{id}/status")
    public CompanyResponse updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody CompanyStatusRequest request) {
        return service.updateStatus(
                id,
                request.status()
        );
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id) {

        service.delete(id);

        return ResponseEntity.noContent().build();
    }
    @RequestMapping(
            value = "/{id}",
            method = RequestMethod.HEAD
    )
    public ResponseEntity<Void> head(
            @PathVariable Long id) {
        service.getById(id);
        return ResponseEntity.ok().build();
    }
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
