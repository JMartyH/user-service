package com.example.userservice.exceptionhandler;

public class UserDataAccessException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UserDataAccessException(String message) {
        super(message);
    }
}

