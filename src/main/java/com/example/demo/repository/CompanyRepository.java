package com.example.demo.repository;

import com.example.demo.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    boolean existsByEmailIgnoreCase(String email);
//////////////////////
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
/////////////
    Optional<Company> findByEmailIgnoreCase(String email);
}