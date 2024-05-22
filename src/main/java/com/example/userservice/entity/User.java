package com.example.userservice.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Entity
@Data
@Table(name = "users")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Email(message = "Invalid email format")
	@NotBlank(message = "Email is required")
	@Column(unique = true)
	private String email;
	@NotBlank(message = "Password is required")
	private String password;
	@NotBlank(message = "First Name is required")
	private String firstName;
	@NotBlank(message = "Last Name is required")
	private String lastName;
	private LocalDateTime registrationDate;

}
