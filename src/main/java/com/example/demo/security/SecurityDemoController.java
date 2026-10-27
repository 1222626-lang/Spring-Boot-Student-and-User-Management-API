
package com.example.demo.security;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SecurityDemoController {

    @GetMapping("/api/public/info")
    public String publicInfo() {
        return "This is public information";
    }

    @GetMapping("/api/student/dashboard")
    public String studentDashboard() {
        return "Welcome to Student Dashboard";
    }

    @GetMapping("/api/admin/dashboard")
    public String adminDashboard() {
        return "Welcome to Admin Dashboard";
    }
}
