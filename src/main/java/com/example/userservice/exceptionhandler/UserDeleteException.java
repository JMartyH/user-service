package com.example.userservice.exceptionhandler;

public class UserDeleteException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public UserDeleteException(String message) {
		super(message);
	}

	
}
