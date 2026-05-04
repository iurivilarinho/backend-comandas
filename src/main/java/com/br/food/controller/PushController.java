package com.br.food.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.br.food.request.PushSubscriptionRequest;
import com.br.food.response.PushDiagnosticsResponse;
import com.br.food.response.PushPublicKeyResponse;
import com.br.food.response.PushTestResponse;
import com.br.food.service.PushNotificationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/push")
@Tag(name = "Push notifications")
public class PushController {

	private final PushNotificationService pushNotificationService;

	public PushController(PushNotificationService pushNotificationService) {
		this.pushNotificationService = pushNotificationService;
	}

	@Operation(summary = "Chave pública VAPID para registro de subscription no front")
	@GetMapping("/public-key")
	public ResponseEntity<PushPublicKeyResponse> getPublicKey() {
		return ResponseEntity.ok(new PushPublicKeyResponse(pushNotificationService.getPublicKey()));
	}

	@Operation(summary = "Registra subscription do navegador (cliente ou cozinha)")
	@PostMapping("/subscriptions")
	public ResponseEntity<Void> subscribe(@Valid @RequestBody PushSubscriptionRequest request) {
		pushNotificationService.saveSubscription(request);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "Remove subscription pelo endpoint informado")
	@DeleteMapping("/subscriptions")
	public ResponseEntity<Void> unsubscribe(@RequestParam("endpoint") String endpoint) {
		pushNotificationService.deleteSubscription(endpoint);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "Diagnostico de push: VAPID, contagem por topico e subscription deste dispositivo")
	@GetMapping("/diagnostics")
	public ResponseEntity<PushDiagnosticsResponse> diagnostics(
			@RequestParam(value = "endpoint", required = false) String endpoint) {
		return ResponseEntity.ok(pushNotificationService.getDiagnostics(endpoint));
	}

	@Operation(summary = "Envia push de teste para o endpoint informado e retorna status do gateway")
	@PostMapping("/test")
	public ResponseEntity<PushTestResponse> sendTest(@RequestParam("endpoint") String endpoint) {
		return ResponseEntity.ok(pushNotificationService.sendTestToEndpoint(endpoint));
	}
}
