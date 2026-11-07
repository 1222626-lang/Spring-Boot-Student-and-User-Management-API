package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public class CompanyPatchRequest {

        @Email(message = "Invalid company email")
        private String email;

        @Size(min = 2, message = "Company name must be at least 2 characters")
        private String name;

        @Size(min = 2, message = "Company address must be at least 2 characters")
        private String address;

        public String getEmail() {
                return email;
        }

        public void setEmail(String email) {
                this.email = email;
        }

        public String getName() {
                return name;
        }

        public void setName(String name) {
                this.name = name;
        }

        public String getAddress() {
                return address;
        }

        public void setAddress(String address) {
                this.address = address;
        }
}