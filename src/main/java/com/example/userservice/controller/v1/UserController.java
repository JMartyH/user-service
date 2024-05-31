package com.example.userservice.controller.v1;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.userservice.dto.UserRegistrationDto;
import com.example.userservice.dto.UserResponseDto;
import com.example.userservice.exceptionhandler.EmailAlreadyExistsException;
import com.example.userservice.exceptionhandler.UserNotFoundException;
import com.example.userservice.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "User Controller", description = "Operations related to users")
@RestController
@RequestMapping("api/v1/users")
public class UserController {

	private static final Logger logger = LoggerFactory.getLogger(UserController.class);

	private UserService userService;

	@Autowired
	public UserController(UserService userService) {
		super();
		this.userService = userService;
	}

	
	@Operation(summary = "Register a new user", description = "Creates a new user account. Sends a welcome email upon successful registration.")
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "201", description = "User created successfully"),
			@ApiResponse(responseCode = "400", description = "Bad Request - Invalid input data"),
			@ApiResponse(responseCode = "409", description = "Conflict - Email already exists") })
	@PostMapping("/register")
	public ResponseEntity<UserResponseDto> registerUser(
			@Parameter(description = "User registration details") @Valid @RequestBody UserRegistrationDto userDto) {
		logger.info("Registering the following user: {}", userDto);
		try {
			UserResponseDto savedUser = userService.registerUser(userDto);
			logger.info("Successfully registered the following user: {}", savedUser);
			return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
		} catch (EmailAlreadyExistsException e) {
	        logger.warn("The email is already in use: {}", userDto.getEmail());
	       return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}
	}

	@Operation(summary = "Get user by ID", description = "Retrieves user details by their ID.")
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "User found"),
			@ApiResponse(responseCode = "404", description = "User not found") })
	@GetMapping("/{id}")
	public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
	    logger.info("Fetching the following user with id: {}", id);
	    try {
	        UserResponseDto userDto = userService.getUserById(id);
	        logger.info("Returning the user with id: {}", id);
	        return ResponseEntity.ok(userDto);
	    } catch (UserNotFoundException e) {
	        logger.warn("User not found with ID: {}", id);
	        return ResponseEntity.notFound().build();
	    }
	}

	@Operation(summary = "Get all users", description = "Retrieves all users in a list.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Retrieved all users successfully"), })
	@GetMapping
	public ResponseEntity<List<UserResponseDto>> getAllUsers() {
		logger.info("Fetching all users");
		List<UserResponseDto> users = userService.getAllUsers();
		logger.info("Returning all users. Total number of users: {}", users.size());
		return ResponseEntity.ok(users);
	}

	@Operation(summary = "Update a user", description = "Updates an existing user.")
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "User updated successfully"),
			@ApiResponse(responseCode = "400", description = "Bad Request - Invalid input data"),
			@ApiResponse(responseCode = "404", description = "User not found"),
			@ApiResponse(responseCode = "409", description = "Conflict - Email already exists") })
	@PutMapping("/{id}")
	public ResponseEntity<UserResponseDto> updateUser(@Parameter(description = "User ID") @PathVariable Long id,
			@Parameter(description = "Updated user details") @Valid @RequestBody UserRegistrationDto userDto) {
		logger.info("Updating the following user with id: {}", id);
		try {
			UserResponseDto updatedUser = userService.updateUser(id, userDto);
			logger.info("User with id: {}, has been successfully updated", id);
			return ResponseEntity.ok(updatedUser);
		} catch (EmailAlreadyExistsException e) {
	        logger.warn("The email is already in use: {}", userDto.getEmail());
	       return ResponseEntity.status(HttpStatus.CONFLICT).build();
		} catch(UserNotFoundException e){
	        logger.warn("User not found with ID: {}", id);
	        return ResponseEntity.notFound().build();
		}
	}

	@Operation(summary = "Delete user by ID", description = "Deletes a user by ID.")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "User deleted successfully"),
			@ApiResponse(responseCode = "404", description = "User not found") })
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteUser(@Parameter(description = "User ID") @PathVariable Long id) {
		logger.info("Deleting the following user with id: {}", id);
		try {
			userService.deleteUser(id);
			logger.info("Deleted user with id: {}", id);
			return ResponseEntity.noContent().build();
		} catch (UserNotFoundException e) {
	        logger.warn("User not found with ID: {}", id);
	        return ResponseEntity.notFound().build();
		}

	}

}
