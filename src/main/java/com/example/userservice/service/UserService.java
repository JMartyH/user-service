package com.example.userservice.service;

import java.util.List;

import com.example.userservice.dto.UserRegistrationDto;
import com.example.userservice.dto.UserResponseDto;
import com.example.userservice.exceptionhandler.EmailAlreadyExistsException;

public interface UserService {
	UserResponseDto registerUser(UserRegistrationDto registrationDto) throws EmailAlreadyExistsException;

	UserResponseDto getUserById(Long userId);

	List<UserResponseDto> getAllUsers();

	UserResponseDto updateUser(Long userId, UserRegistrationDto userDto);

	void deleteUser(Long userId);

}
