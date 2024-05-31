package com.example.userservice.exceptionhandler;

public class UserUpdateException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public UserUpdateException(String message) {
		super(message);
	}

}
