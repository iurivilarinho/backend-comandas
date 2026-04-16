package com.br.food.authentication.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.br.food.authentication.response.UserProfileResponse;
import com.br.food.models.User;
import com.br.food.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;

@RestController
@Validated
@RequestMapping("/user")
@Tag(name = "User Profile", description = "Endpoints do usuario autenticado")
public class UserProfileController {

	private final UserService userService;

	public UserProfileController(UserService userService) {
		this.userService = userService;
	}

	@Operation(summary = "Consultar usuario autenticado")
	@GetMapping("/me")
	public ResponseEntity<UserProfileResponse> me() {
		User user = userService.findUserLoggedIn();

		if (user == null) {
			throw new EntityNotFoundException("Usuario autenticado nao encontrado.");
		}

		return ResponseEntity.ok(new UserProfileResponse(user));
	}
}
