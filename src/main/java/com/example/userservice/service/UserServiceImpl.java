package com.example.userservice.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.userservice.dto.UserRegistrationDto;
import com.example.userservice.dto.UserResponseDto;
import com.example.userservice.entity.User;
import com.example.userservice.exceptionhandler.EmailAlreadyExistsException;
import com.example.userservice.exceptionhandler.UserDataAccessException;
import com.example.userservice.exceptionhandler.UserDeleteException;
import com.example.userservice.exceptionhandler.UserNotFoundException;
import com.example.userservice.exceptionhandler.UserUpdateException;
import com.example.userservice.repository.UserRepository;

@Service
public class UserServiceImpl implements UserService {

	private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

	private UserRepository userRepository;

	private PasswordEncoder passwordEncoder;

	private JavaMailSender mailSender;

	private static final String USER_NOT_FOUND = "User not found with id: ";

	@Autowired
	public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JavaMailSender mailSender) {
		super();
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.mailSender = mailSender;
	}

	@Override
	public UserResponseDto registerUser(UserRegistrationDto registrationDto) throws EmailAlreadyExistsException {
		logger.info("Registering new user with email: {}", registrationDto.getEmail());
		if (userRepository.findByEmail(registrationDto.getEmail()).isPresent()) {
			throw new EmailAlreadyExistsException("Email already exists: " + registrationDto.getEmail());
		}

		logger.info("Hashing the password...");
		// Hashing the password
		String hashedPassword = passwordEncoder.encode(registrationDto.getPassword());
		logger.info("Password hashed successfully!");
		User user = new User();
		user.setEmail(registrationDto.getEmail());
		user.setPassword(hashedPassword);
		user.setFirstName(registrationDto.getFirstName());
		user.setLastName(registrationDto.getLastName());
		user.setRegistrationDate(LocalDateTime.now());

		User savedUser = userRepository.save(user);
		logger.info("User registered successfully with ID: {}", savedUser.getId());
		// Send welcome email
		sendWelcomeEmail(savedUser);
		return mapToDto(savedUser);
	}

	@Override
	public UserResponseDto getUserById(Long userId) {
		logger.info("Fetching user with ID: {}", userId);
		Optional<User> userOptional = userRepository.findById(userId);
		if (userOptional.isEmpty()) {
			logger.warn("User not found with ID: {}", userId);
			throw new UserNotFoundException(USER_NOT_FOUND + userId);
		}
		return mapToDto(userOptional.get());
	}

	@Override
	public List<UserResponseDto> getAllUsers() {
		logger.info("Fetching all users");

		try {
			// Retrieve all users from the database
			List<User> users = userRepository.findAll();
			logger.debug("Found {} users", users.size());

			// Convert users to UserResponseDto objects
			List<UserResponseDto> userDtos = users.stream().map(this::mapToDto).toList();

			logger.info("Successfully retrieved all users");
			return userDtos;

		} catch (DataAccessException e) {
			logger.error("Error fetching all users: {}", e.getMessage(), e);
			throw new UserDataAccessException("Error accessing user data"); // Use custom exception
		}
	}

	@Override
	public UserResponseDto updateUser(Long userId, UserRegistrationDto userDto) {
		logger.info("Updating user with ID: {}", userId);

		User existingUser = userRepository.findById(userId)
				.orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND + userId));

		try {
			// Check and update fields only if they are not null or empty
			if (userDto.getFirstName() != null && !userDto.getFirstName().isEmpty()) {
				existingUser.setFirstName(userDto.getFirstName());
			}

			if (userDto.getLastName() != null && !userDto.getLastName().isEmpty()) {
				existingUser.setLastName(userDto.getLastName());
			}

			if (userDto.getEmail() != null && !userDto.getEmail().isEmpty()
					&& !existingUser.getEmail().equals(userDto.getEmail())) {
				if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
					throw new EmailAlreadyExistsException("Email already exists: " + userDto.getEmail());
				}
				existingUser.setEmail(userDto.getEmail());
			}

			if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
				logger.info("Hashing the password...");
				String hashedNewPassword = passwordEncoder.encode(userDto.getPassword());
				existingUser.setPassword(hashedNewPassword);
				logger.info("Password hashed successfully!");
			}

			User updatedUser = userRepository.save(existingUser);
			logger.info("User with ID: {} updated successfully", userId);
			return mapToDto(updatedUser);
		} catch (EmailAlreadyExistsException ex) {

			// Re-throw so the GlobalExceptionHandler can handle it
			throw ex;

		} catch (Exception e) {
			logger.error("Error updating user with ID: {} - {}", userId, e.getMessage(), e);
			throw new UserUpdateException("Error updating user");
		}
	}

	@Override
	public void deleteUser(Long userId) {
		logger.info("Deleting user with ID: {}", userId);

		try {
			if (userRepository.existsById(userId)) {
				userRepository.deleteById(userId);
				logger.info("User with ID: {} deleted successfully", userId);
			} else {
				logger.warn("User with ID: {} not found", userId);
				throw new UserNotFoundException(USER_NOT_FOUND + userId);
			}
		} catch (UserNotFoundException ex) {
			throw ex;
		} catch (Exception e) {
			logger.error("Error deleting user with ID: {} - {}", userId, e.getMessage(), e);
			throw new UserDeleteException("Error deleting user");
		}
	}

	private UserResponseDto mapToDto(User user) {
	    return new UserResponseDto(
	        user.getId(),
	        user.getEmail(),
	        user.getFirstName(),
	        user.getLastName(),
	        user.getRegistrationDate()
	    );
	}
	public void sendWelcomeEmail(User user) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom("noreply@userservice.com");
		message.setTo(user.getEmail());
		message.setSubject("Welcome to User Service!");
		message.setText("Thank you for registering, " + user.getFirstName() + "!");

		logger.info("Sending welcome email to {}", user.getEmail());
		mailSender.send(message);
		logger.info("Welcome email sent successfully to {}", user.getEmail());
	}

}
