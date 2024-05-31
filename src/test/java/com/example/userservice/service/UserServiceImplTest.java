package com.example.userservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.userservice.dto.UserRegistrationDto;
import com.example.userservice.dto.UserResponseDto;
import com.example.userservice.entity.User;
import com.example.userservice.exceptionhandler.EmailAlreadyExistsException;
import com.example.userservice.exceptionhandler.UserDataAccessException;
import com.example.userservice.exceptionhandler.UserDeleteException;
import com.example.userservice.exceptionhandler.UserNotFoundException;
import com.example.userservice.exceptionhandler.UserUpdateException;
import com.example.userservice.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

	@Mock
	private UserRepository userRepository;
	@Mock
	private PasswordEncoder passwordEncoder;
	@Mock
	private JavaMailSender javaMailSender;

	@InjectMocks
	private UserServiceImpl userService;

	private UserRegistrationDto userRegistrationDto;
	private User user;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);

		Long userId = 1L;

		userRegistrationDto = new UserRegistrationDto();
		userRegistrationDto.setEmail("test@userservice.com");
		userRegistrationDto.setPassword("password");
		userRegistrationDto.setFirstName("John");
		userRegistrationDto.setLastName("Her");

		user = new User(userRegistrationDto, passwordEncoder);
		user.setId(userId);
		user.setRegistrationDate(LocalDateTime.now());
	}

	@Test
	void testRegisterUserSuccess() {
		Long userId = 1L;
		when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
		when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
			User user = invocation.getArgument(0);
			user.setId(userId);
			return user;
		});
		UserResponseDto result = userService.registerUser(userRegistrationDto);
		assertNotNull(result);
		assertEquals(userRegistrationDto.getEmail(), result.getEmail());
		assertEquals(userRegistrationDto.getFirstName(), result.getFirstName());
		assertEquals(userRegistrationDto.getLastName(), result.getLastName());
		assertNotEquals(user.getPassword(), userRegistrationDto.getPassword());
		verify(javaMailSender).send(any(SimpleMailMessage.class));

	}

	@Test
	void testRegisterUserEmailAlreadyExists() {
		when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
		assertThrows(EmailAlreadyExistsException.class, () -> userService.registerUser(userRegistrationDto));
	}

	@Test
	void testGetUserByIdSuccess() {
		Long userId = 1L;

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));

		UserResponseDto result = userService.getUserById(1L);
		assertNotNull(result);
		assertEquals(userId, result.getId().longValue());
		assertEquals(user.getEmail(), result.getEmail());
		assertEquals(user.getFirstName(), result.getFirstName());
		assertEquals(user.getLastName(), result.getLastName());
		assertEquals(user.getRegistrationDate(), result.getRegistrationDate());
	}

	@Test
	void testGetUserByIdNotFound() {
		Long userId = 1L;

		when(userRepository.findById(1L)).thenReturn(Optional.empty());
		assertThrows(UserNotFoundException.class, () -> userService.getUserById(userId));
	}

	@Test
	void testGetAllUsersSuccess() {

		List<User> mockUsers = new ArrayList<>();
		mockUsers.add(user);
		mockUsers.add(
				new User(new UserRegistrationDto("a@userservice.com", "password", "Jane", "Doe"), passwordEncoder));

		when(userRepository.findAll()).thenReturn(mockUsers);

		List<UserResponseDto> result = userService.getAllUsers();

		assertEquals(mockUsers.size(), result.size());

		for (int i = 0; i < mockUsers.size(); i++) {

			UserResponseDto userDto = result.get(i);
			User originalUser = mockUsers.get(i);
			assertEquals(originalUser.getId(), userDto.getId());
			assertEquals(originalUser.getEmail(), userDto.getEmail());
			assertEquals(originalUser.getFirstName(), userDto.getFirstName());
			assertEquals(originalUser.getLastName(), userDto.getLastName());
			assertEquals(originalUser.getRegistrationDate(), userDto.getRegistrationDate());
		}
	}

	@Test
	void testGetAllUsers_DataAccessException() {

		when(userRepository.findAll()).thenThrow(new DataAccessException("Simulated database error") {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
		});

		assertThrows(UserDataAccessException.class, () -> userService.getAllUsers(),
				"Expected getAllUsers() to throw RuntimeException when DataAccessException occurs");
	}

	@Test
	void testUpdateUserSuccess() {
		Long userId = 1L;
		// Setup
		UserRegistrationDto updatedDto = new UserRegistrationDto();
		updatedDto.setEmail("update@userservice.com");
		updatedDto.setFirstName("updated first name");
		updatedDto.setLastName("updated last name");
		updatedDto.setPassword("updatedpassword");

		// Stubs
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

		UserResponseDto result = userService.updateUser(userId, updatedDto);

		assertNotNull(result);
		assertEquals(updatedDto.getEmail(), result.getEmail());
		assertEquals(updatedDto.getFirstName(), result.getFirstName());
		assertEquals(updatedDto.getLastName(), result.getLastName());

	}

	@Test
	void testUpdateUser_EmailAlreadyExists() {
		Long userId = 1L;
		UserRegistrationDto updatedDto = new UserRegistrationDto();
		updatedDto.setEmail("existing@example.com"); // An email that already exists

		// Make the repository return the user object when findById() is called with the
		// specified ID.
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));

		// Simulate that an user with the updated email already exists.
		when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

		assertThrows(EmailAlreadyExistsException.class, () -> userService.updateUser(userId, updatedDto));

		// (Optional) Verify that userRepository.save() is never called
		verify(userRepository, never()).save(any());
	}

	@Test
	void testUpdateUser_UnexpectedError() {
		Long userId = 1L;
		UserRegistrationDto updatedDto = new UserRegistrationDto(); // ... set up the DTO

		// Simulate an error during saving
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("Database error"));

		assertThrows(UserUpdateException.class, () -> userService.updateUser(userId, updatedDto));
	}

	@Test
	void testDeleteUserSuccess() {
		Long userId = 1L;

		when(userRepository.existsById(userId)).thenReturn(true);

		userService.deleteUser(userId);
		verify(userRepository).deleteById(userId);
	}

	@Test
	void testDeleteUserNotFound() {
		Long userId = 1L;

		assertThrows(UserNotFoundException.class, () -> userService.deleteUser(userId));

		// Verify that deleteById wasn't called
		verify(userRepository, never()).deleteById(anyLong());
	}

	@Test
	void testDeleteUser_UnexpectedError() {
		// Arrange
		Long userId = 1L;
		when(userRepository.existsById(userId)).thenReturn(true); // Simulate user exists
		doThrow(new RuntimeException("Database error")).when(userRepository).deleteById(userId); // Simulate error
																									// during deletion

		// Act & Assert
		assertThrows(UserDeleteException.class, () -> userService.deleteUser(userId));
	}

}
