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
@Tag(name = "RecuperaÃ§Ã£o de Senha", description = "Endpoints para envio e validaÃ§Ã£o de cÃ³digo e redefiniÃ§Ã£o de senha.")
public class PasswordRecoveryController {

	private final PasswordRecoveryService passwordRecoveryService;

	public PasswordRecoveryController(PasswordRecoveryService passwordRecoveryService) {
		this.passwordRecoveryService = passwordRecoveryService;
	}

	@PostMapping("/send")
	@Operation(summary = "Enviar cÃ³digo de recuperaÃ§Ã£o por e-mail", description = "Gera um cÃ³digo de 4 dÃ­gitos e envia para o e-mail informado.")
	@ApiResponse(responseCode = "204", description = "E-mail de recuperaÃ§Ã£o enviado com sucesso.")
	@ApiResponse(responseCode = "403", description = "NÃ£o foi possÃ­vel enviar o e-mail de recuperaÃ§Ã£o.", content = @Content)
	public ResponseEntity<Void> send(@RequestBody SendRecoveryEmailRequest body) {
		passwordRecoveryService.sendRecoveryEmail(body.getEmail());
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/validate")
	@Operation(summary = "Validar cÃ³digo de recuperaÃ§Ã£o", description = "Valida se o cÃ³digo informado estÃ¡ correto e dentro do prazo de expiraÃ§Ã£o para o e-mail.")
	@ApiResponse(responseCode = "204", description = "CÃ³digo vÃ¡lido.")
	@ApiResponse(responseCode = "403", description = "CÃ³digo invÃ¡lido ou expirado.", content = @Content)
	public ResponseEntity<Void> validate(@RequestBody ValidateRecoveryCodeRequest body) {
		passwordRecoveryService.validateCode(body.getCode(), body.getEmail());
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/reset")
	@Operation(summary = "Redefinir senha", description = "Redefine a senha do usuÃ¡rio apÃ³s validar o cÃ³digo informado.")
	@ApiResponse(responseCode = "204", description = "Senha redefinida com sucesso.")
	@ApiResponse(responseCode = "403", description = "CÃ³digo invÃ¡lido ou expirado.", content = @Content)
	public ResponseEntity<Void> reset(@RequestBody @Valid RecoveryPasswordRequest body) {
		passwordRecoveryService.resetPassword(body);
		return ResponseEntity.noContent().build();
	}
}
