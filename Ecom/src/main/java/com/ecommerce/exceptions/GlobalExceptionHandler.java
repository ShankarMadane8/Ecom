package com.ecommerce.exceptions;

import com.ecommerce.response.ResponseHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<?> handleUserNotFoundException(UserNotFoundException ex, WebRequest request) {
       return ResponseHandler.generateResponse(HttpStatus.NOT_FOUND,ex.getMessage(),null);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<?> handleUserNotFoundException(UserAlreadyExistsException ex, WebRequest request) {
        return ResponseHandler.generateResponse(HttpStatus.CONFLICT,ex.getMessage(),null);
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<?> handleUserNotFoundException(CategoryNotFoundException ex, WebRequest request) {
        return ResponseHandler.generateResponse(HttpStatus.NOT_FOUND,ex.getMessage(),null);
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<?> handleUserNotFoundException(OrderNotFoundException ex, WebRequest request) {
        return ResponseHandler.generateResponse(HttpStatus.NOT_FOUND,ex.getMessage(),null);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        // Extract validation error messages
        StringBuilder errorMessage = new StringBuilder();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String defaultMessage = error.getDefaultMessage();
            if (defaultMessage != null) {
                errorMessage.append(defaultMessage).append(" ");
            }
        });

        return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, errorMessage.toString().trim(), null);
    }

//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<?> handleUserNotFoundException(Exception ex, WebRequest request) {
//        return ResponseHandler.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR,"An unexpected error occurred: " + ex.getMessage(),null);
//    }
}
