package com.example.userservice.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.userservice.controller.v1.UserController;
import com.example.userservice.dto.UserRegistrationDto;
import com.example.userservice.dto.UserResponseDto;
import com.example.userservice.exceptionhandler.EmailAlreadyExistsException;
import com.example.userservice.exceptionhandler.UserNotFoundException;
import com.example.userservice.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

	@Mock
	private UserService userService;

	@InjectMocks
	private UserController userController;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

	private UserResponseDto userResponseDto;
	private UserRegistrationDto userRegistrationDto;
	

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
		objectMapper = new ObjectMapper();

		userResponseDto = new UserResponseDto(1L,"test@userservice.com", "John", "Her", LocalDateTime.now());

		userRegistrationDto = new UserRegistrationDto();
		userRegistrationDto.setEmail("test@userservice.com");
		userRegistrationDto.setPassword("password");
		userRegistrationDto.setFirstName("John");
		userRegistrationDto.setLastName("Her");
	}

	@Test
	@WithMockUser
	void testRegisterUser() throws Exception {

		// In this stub, we mock register a user and it should return the
		// userResponseDto that is in the set up
		when(userService.registerUser(any(UserRegistrationDto.class))).thenReturn(userResponseDto);

		mockMvc.perform(post("/api/v1/users/register").with(csrf()).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(userRegistrationDto))).andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(userResponseDto.getId()))
				.andExpect(jsonPath("$.email").value(userResponseDto.getEmail()))
				.andExpect(jsonPath("$.firstName").value(userResponseDto.getFirstName()))
				.andExpect(jsonPath("$.lastName").value(userResponseDto.getLastName()))
				.andDo(MockMvcResultHandlers.print());

	}

	@Test
	@WithMockUser
	void testRegisterUser_withInvalidEmail_returnsBadRequest() throws Exception {

		userRegistrationDto = new UserRegistrationDto();
		userRegistrationDto.setEmail("not an email");
		userRegistrationDto.setPassword("password");
		userRegistrationDto.setFirstName("John");
		userRegistrationDto.setLastName("Her");

		mockMvc.perform(post("/api/v1/users/register").with(csrf()).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(userRegistrationDto))).andExpect(status().isBadRequest())
				.andDo(MockMvcResultHandlers.print());
	}

	@Test
	@WithMockUser
	void testRegisterUser_EmailAlreadyExists() throws Exception {
		
		userRegistrationDto.setEmail("existing@userservice.com");

		when(userService.registerUser(userRegistrationDto))
				.thenThrow(new EmailAlreadyExistsException("Email already exists"));
		mockMvc.perform(post("/api/v1/users/register").with(csrf()).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(userRegistrationDto))).andExpect(status().isConflict())
				.andDo(MockMvcResultHandlers.print());
	}

	@Test
	@WithMockUser
	void testGetUserById() throws Exception {

		when(userService.getUserById(anyLong())).thenReturn(userResponseDto);

		mockMvc.perform(get("/api/v1/users/1")).andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(userResponseDto.getId()))
				.andExpect(jsonPath("$.email").value(userResponseDto.getEmail()))
				.andExpect(jsonPath("$.firstName").value(userResponseDto.getFirstName()))
				.andExpect(jsonPath("$.lastName").value(userResponseDto.getLastName()))
				.andDo(MockMvcResultHandlers.print());

	}

	@Test
	@WithMockUser // Ensure user is authenticated for protected endpoints
	void testGetUserById_NotFound() throws Exception {
		Long userId = 1L;

		when(userService.getUserById(userId)).thenThrow(new UserNotFoundException("User not found"));

		mockMvc.perform(get("/api/v1/users/{id}", userId)).andExpect(status().isNotFound())
				.andDo(MockMvcResultHandlers.print());

	}

	@Test
	@WithMockUser
	void testGetAllUsers() throws Exception {

		List<UserResponseDto> users = Collections.singletonList(userResponseDto);

		when(userService.getAllUsers()).thenReturn(users);

		mockMvc.perform(get("/api/v1/users")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].id").value(userResponseDto.getId()))
				.andExpect(jsonPath("$[0].email").value(userResponseDto.getEmail()))
				.andExpect(jsonPath("$[0].firstName").value(userResponseDto.getFirstName()))
				.andExpect(jsonPath("$[0].lastName").value(userResponseDto.getLastName()))
				.andDo(MockMvcResultHandlers.print());

	}

	@Test
	@WithMockUser
	void testUpdateUser_Success() throws Exception {
		// Setup for updated user information
		UserRegistrationDto updatedDto = new UserRegistrationDto();
		updatedDto.setEmail("updated@userservice.com");
		updatedDto.setPassword("updated password");
		updatedDto.setFirstName("update firstname");
		updatedDto.setLastName("updated lastname");
		// Setup for the response of the application
		UserResponseDto updatedUser = new UserResponseDto(1L, "updated@userservice.com", "update firstname", "updated lastname", LocalDateTime.now());

		when(userService.updateUser(1L, updatedDto)).thenReturn(updatedUser);

		mockMvc.perform(put("/api/v1/users/{id}", 1L).with(csrf()).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updatedDto))).andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value(updatedDto.getEmail()))
				.andExpect(jsonPath("$.firstName").value(updatedDto.getFirstName()))
				.andExpect(jsonPath("$.lastName").value(updatedDto.getLastName())).andDo(MockMvcResultHandlers.print());
	}
	
	@Test
	@WithMockUser
	void testUpdateUser_EmailAlreadyExists() throws Exception {
		Long userId = 1L;
		
		// Setup for updated user information
		UserRegistrationDto updatedDto = new UserRegistrationDto();
		updatedDto.setEmail("updated@userservice.com");
		updatedDto.setPassword("updated password");
		updatedDto.setFirstName("update firstname");
		updatedDto.setLastName("updated lastname");
		// Setup for the response of the application
		//UserResponseDto updatedUser = new UserResponseDto(userId, "updated@userservice.com", "update firstname", "updated lastname", LocalDateTime.now());

		when(userService.updateUser(userId, updatedDto)).thenThrow(new EmailAlreadyExistsException("Email already exists"));

		mockMvc.perform(put("/api/v1/users/{id}", userId).with(csrf()).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updatedDto)))
		.andExpect(status().isConflict())
		.andDo(MockMvcResultHandlers.print());
	}

	@Test
	@WithMockUser
	void testDeleteUser() throws Exception {
		mockMvc.perform(delete("/api/v1/users/{id}", 1L).with(csrf())).andExpect(status().isNoContent());
	}

	@Test
	@WithMockUser
	void testDeleteUserNotFOund() throws Exception {

		Long userId = 2L;

		doThrow(UserNotFoundException.class).when(userService).deleteUser(userId);

		mockMvc.perform(delete("/api/v1/users/{id}", userId).with(csrf()))
				.andExpect(status().isNotFound())
				.andDo(MockMvcResultHandlers.print());
	}

}
