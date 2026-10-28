package com.br.food.authentication.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.br.food.authentication.PasswordRecoveryService;
import com.br.food.authentication.request.RecoveryPasswordRequest;
import com.br.food.authentication.request.SendRecoveryEmailRequest;
import com.br.food.authentication.request.ValidateRecoveryCodeRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth/password-recovery")
@Tag(name = "Recuperação de Senha", description = "Endpoints para envio e validação de código e redefinição de senha.")
public class PasswordRecoveryController {

	private final PasswordRecoveryService passwordRecoveryService;

	public PasswordRecoveryController(PasswordRecoveryService passwordRecoveryService) {
		this.passwordRecoveryService = passwordRecoveryService;
	}

	@PostMapping("/send")
	@Operation(summary = "Enviar código de recuperação por e-mail", description = "Gera um código de 4 dígitos e envia para o e-mail informado.")
	@ApiResponse(responseCode = "204", description = "E-mail de recuperação enviado com sucesso.")
	@ApiResponse(responseCode = "403", description = "Não foi possível enviar o e-mail de recuperação.", content = @Content)
	public ResponseEntity<Void> send(@RequestBody SendRecoveryEmailRequest body) {
		passwordRecoveryService.sendRecoveryEmail(body.getEmail());
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/validate")
	@Operation(summary = "Validar código de recuperação", description = "Valida se o código informado está correto e dentro do prazo de expiração para o e-mail.")
	@ApiResponse(responseCode = "204", description = "Código válido.")
	@ApiResponse(responseCode = "403", description = "Código inválido ou expirado.", content = @Content)
	public ResponseEntity<Void> validate(@RequestBody ValidateRecoveryCodeRequest body) {
		passwordRecoveryService.validateCode(body.getCode(), body.getEmail());
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/reset")
	@Operation(summary = "Redefinir senha", description = "Redefine a senha do usuário após validar o código informado.")
	@ApiResponse(responseCode = "204", description = "Senha redefinida com sucesso.")
	@ApiResponse(responseCode = "403", description = "Código inválido ou expirado.", content = @Content)
	public ResponseEntity<Void> reset(@RequestBody @Valid RecoveryPasswordRequest body) {
		passwordRecoveryService.resetPassword(body);
		return ResponseEntity.noContent().build();
	}
}
