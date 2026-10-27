
package com.example.demo.service;

import com.example.demo.dto.StudentRequest;
import com.example.demo.dto.StudentResponse;
import com.example.demo.entity.Student;
import com.example.demo.exception.StudentNotFoundException;
import com.example.demo.mapper.StudentMapper;
import com.example.demo.repository.StudentRepository;
import com.example.demo.support.StudentSupportService;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

        super(studentRepository, studentMapper);

        this.studentRepository = studentRepository;
        this.supportService = supportService;
    }

    // Preserve the existing API method
    @Transactional(readOnly = true)
    public List<StudentResponse> getAllStudents() {
        return mapper.toResponseList(studentRepository.findAll());
    }

    @Transactional
    public StudentResponse createStudent(StudentRequest request) {
        return create(request);
    }

    @Transactional
    public StudentResponse updateStudent(
            Long id, StudentRequest request) {
        return update(id, request);
    }

    @Transactional
    public StudentResponse patchStudent(
            Long id, Map<String, Object> updates) {

        supportService.validatePatchUpdates(updates);

        Student existing = studentRepository.findById(id)
                .orElseThrow(() -> new StudentNotFoundException(id));

        for (Map.Entry<String, Object> entry : updates.entrySet()) {
            String value = (String) entry.getValue();

            switch (entry.getKey()) {
                case "firstName" -> existing.setFirstName(value);
                case "lastName" -> existing.setLastName(value);
                case "email" -> existing.setEmail(value);
                default -> throw new IllegalArgumentException(
                        "Unknown field: " + entry.getKey());
            }
        }

        return mapper.toResponse(studentRepository.save(existing));
    }

    @Transactional
    public void deleteStudent(Long id) {
        if (!studentRepository.existsById(id)) {
            throw new StudentNotFoundException(id);
        }

        studentRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public boolean studentExists(Long id) {
        return studentRepository.existsById(id);
    }

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
