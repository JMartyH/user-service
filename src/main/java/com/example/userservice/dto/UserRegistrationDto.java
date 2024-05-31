package com.example.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Registration DTO for user details")
public class UserRegistrationDto {

    @Schema(description = "Email address of the user", example = "user@example.com")
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @Schema(description = "Password of the user. Minimum of 8 characters.")
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @Schema(description = "First Name")
    @NotBlank(message = "First Name is required")
    private String firstName;

    @Schema(description = "Last Name")
    @NotBlank(message = "Last Name is required")
    private String lastName;
}
