package com.pmo.nid.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidationExceptions(
      MethodArgumentNotValidException ex, WebRequest request) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach((error) -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      errors.put(fieldName, errorMessage);
    });

    var output = Map.of("timestamp", LocalDateTime.now(), "message", "validation error", "errors", errors);
    return new ResponseEntity<>(output, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<ErrorDetails> handleBadRequestException(BadRequestException ex, WebRequest request) {
    return new ResponseEntity<>(generateErrorDetails(ex.getMessage(), request), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorDetails> handleGenericException(Exception ex, WebRequest request) {
    var msg = ex.getMessage();
    return new ResponseEntity<>(generateErrorDetails(msg, request), HttpStatus.INTERNAL_SERVER_ERROR);
  }

  private ErrorDetails generateErrorDetails(String message, WebRequest request) {
    return new ErrorDetails(LocalDateTime.now(), message, request.getDescription(false));
  }
}
