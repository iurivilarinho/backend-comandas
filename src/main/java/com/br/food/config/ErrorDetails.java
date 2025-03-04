package com.br.food.config;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.persistence.EntityNotFoundException;

@RestControllerAdvice
public class ErrorDetails {

	public class ErrorResponse {

		private LocalDateTime timestamp = LocalDateTime.now();
		private List<String> message;

		public ErrorResponse(List<String> message) {
			this.message = message;

		}

		public List<String> getMessage() {
			return message;
		}

		public void setMessage(List<String> message) {
			this.message = message;
		}

		public LocalDateTime getTimestamp() {
			return timestamp;
		}

	}

	@ExceptionHandler(EntityNotFoundException.class)
	public ResponseEntity<?> tratarErro400(EntityNotFoundException ex) {
		ErrorResponse error = new ErrorResponse(Arrays.asList(ex.getMessage()));
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);

	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<?> tratarErro409(DataIntegrityViolationException ex) {
		ErrorResponse error = new ErrorResponse(Arrays.asList(ex.getMessage()));
		return ResponseEntity.status(HttpStatus.CONFLICT).body(error);

	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<?> tratarErro403(AccessDeniedException ex) {
		ErrorResponse error = new ErrorResponse(Arrays.asList(ex.getMessage()));
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);

	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<?> tratarErro403(IllegalArgumentException ex) {
		ErrorResponse error = new ErrorResponse(Arrays.asList(ex.getMessage()));
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);

	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
		List<String> errors = new ArrayList<>();
		ex.getBindingResult().getAllErrors().forEach((error) -> {
			String errorMessage = error.getDefaultMessage();
			errors.add(errorMessage);
		});
		ErrorResponse error = new ErrorResponse(errors);

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
	}

}
