package com.example.userservice.dto;

import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

//Use @Getter to generate only getters
@Getter
@NoArgsConstructor  
@AllArgsConstructor 
@Schema(description = "Response DTO for user details")
public class UserResponseDto {
	
    @Schema(description = "ID of user")
    private Long id;

    @Schema(description = "Email address of the user", example = "example@example.com")
    private String email;

    @Schema(description = "First Name")
    private String firstName;

    @Schema(description = "Last Name")
    private String lastName;

    @Schema(description = "Registration date of the user")
    private LocalDateTime registrationDate;

}