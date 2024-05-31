package com.example.userservice.exceptionhandler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleEmailAlreadyExistsException(WebRequest request, EmailAlreadyExistsException ex) {

        ApiError apiError = new ApiError(HttpStatus.CONFLICT, ex.getMessage(), LocalDateTime.now());
        apiError.setErrorCode("EMAIL_ALREADY_EXISTS"); 
        
        if (log.isErrorEnabled()) { // Check if the log is configured for ERROR level
            log.error("EmailAlreadyExistsException: {} - Request URI: {} - Exception: {}", 
                         ex.getMessage(), 
                         request.getDescription(false), 
                         ex.getClass().getName());

            if (log.isDebugEnabled()) {
                log.debug("Exception stack trace:", ex); 
            }
        }
        
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiError> handleUserNotFoundException(WebRequest request, UserNotFoundException ex) {
    	
        ApiError apiError = new ApiError(HttpStatus.NOT_FOUND, ex.getMessage(), LocalDateTime.now());
        apiError.setErrorCode("USER_NOT_FOUND"); 
        
        if (log.isErrorEnabled()) { // Check if the log is configured for ERROR level
            log.error("UserNotFoundException: {} - Request URI: {} - Exception: {}", 
                         ex.getMessage(), 
                         request.getDescription(false), 
                         ex.getClass().getName());

            if (log.isDebugEnabled()) {
                log.debug("Exception stack trace:", ex); 
            }
        }
        
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }
    
    @ExceptionHandler(UserUpdateException.class)
    public ResponseEntity<ApiError> handleUserUpdateException(WebRequest request, UserUpdateException ex) {

        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage(), LocalDateTime.now());
        apiError.setErrorCode("USER_UPDATE_ERROR");
        
        if (log.isErrorEnabled()) { // Check if the log is configured for ERROR level
            log.error("UserUpdateException: {} - Request URI: {} - Exception: {}", 
                         ex.getMessage(), 
                         request.getDescription(false), 
                         ex.getClass().getName());

            if (log.isDebugEnabled()) {
                log.debug("Exception stack trace:", ex); 
            }
        }
        
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }

    @ExceptionHandler(UserDeleteException.class)
    public ResponseEntity<ApiError> handleUserDeleteException(WebRequest request, UserDeleteException ex) {

        ApiError apiError = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), LocalDateTime.now());
        apiError.setErrorCode("USER_DELETE_ERROR");
        
        if (log.isErrorEnabled()) { // Check if the log is configured for ERROR level
            log.error("UserDeleteException: {} - Request URI: {} - Exception: {}", 
                         ex.getMessage(), 
                         request.getDescription(false), 
                         ex.getClass().getName());

            if (log.isDebugEnabled()) {
                log.debug("Exception stack trace:", ex); 
            }
        }
        
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }
    
    @ExceptionHandler(UserDataAccessException.class)
    public ResponseEntity<ApiError> handleUserDataAccessException(WebRequest request, UserDataAccessException ex) {
    	
        ApiError apiError = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), LocalDateTime.now());
        apiError.setErrorCode("USER_DATA_ACCESS_ERROR");
        
        if (log.isErrorEnabled()) { // Check if the log is configured for ERROR level
            log.error("UserDataAccessException: {} - Request URI: {} - Exception: {}", 
                         ex.getMessage(), 
                         request.getDescription(false), 
                         ex.getClass().getName());

            if (log.isDebugEnabled()) {
                log.debug("Exception stack trace:", ex); 
            }
        }
        
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }
    

    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request) {

        List<String> errorMessages = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());

        String errorMessage = "Validation error: " + String.join(", ", errorMessages);
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, errorMessage, LocalDateTime.now());
        apiError.setErrorCode("VALIDATION_ERROR");
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGlobalException(WebRequest request, Exception ex) {
        ApiError apiError = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", LocalDateTime.now());
        apiError.setErrorCode("INTERNAL_SERVER_ERROR");
        
        if (log.isErrorEnabled()) { // Check if the log is configured for ERROR level
            log.error("An unexpected error occurred: {} - Request URI: {} - Exception: {}", 
                         ex.getMessage(), 
                         request.getDescription(false), 
                         ex.getClass().getName());

            if (log.isDebugEnabled()) {
                log.debug("Exception stack trace:", ex); 
            }
        }

        return new ResponseEntity<>(apiError, apiError.getStatus());
    }

}
