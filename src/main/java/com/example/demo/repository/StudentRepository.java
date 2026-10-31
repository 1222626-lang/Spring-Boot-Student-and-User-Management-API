
package com.example.demo.repository;

import com.example.demo.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {

    // Check whether an email already exists
    boolean existsByEmailIgnoreCase(String email);

    // Check whether an email belongs to another student
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}
