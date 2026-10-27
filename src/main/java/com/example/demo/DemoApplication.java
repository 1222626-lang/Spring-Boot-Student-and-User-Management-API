
package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {

	private static final String PROJECT_NAME =
			"Spring Boot Student and User Management API";

	private static final String GITHUB_URL =
			"https://github.com/1222626-lang/Spring-Boot-Student-and-User-Management-API";

	public static void main(String[] args) {

		SpringApplication.run(DemoApplication.class, args);

		System.out.println();
		System.out.println("==================================================");
		System.out.println("       WELCOME TO " + PROJECT_NAME);
		System.out.println("==================================================");
		System.out.println();
		System.out.println("Project Description:");
		System.out.println("A RESTful API for managing students and users.");
		System.out.println();
		System.out.println("Main Features:");
		System.out.println("- CRUD Operations");
		System.out.println("- Pagination");
		System.out.println("- Input Validation");
		System.out.println("- JWT Authentication and Authorization");
		System.out.println("- Role-Based Access Control");
		System.out.println("- MySQL Database Integration");
		System.out.println("- DTOs and MapStruct");
		System.out.println("- Spring Data JPA");
		System.out.println();
		System.out.println("GitHub Repository:");
		System.out.println(GITHUB_URL);
		System.out.println();
		System.out.println("Application is starting. Enjoy exploring!");
		System.out.println("==================================================");

	}
}
