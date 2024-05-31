package com.example.userservice.exceptionhandler;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;

import lombok.Data;

@Data
public class ApiError {

	private HttpStatus status;
	private String message;
	private LocalDateTime timestamp;
	private String errorCode;
	
	public ApiError(HttpStatus status, String message, LocalDateTime timestamp) {
		super();
		this.status = status;
		this.message = message;
		this.timestamp = timestamp;
	}
	
}
