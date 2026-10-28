package com.br.food.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.br.food.request.PushSubscriptionRequest;
import com.br.food.response.PushDiagnosticsResponse;
import com.br.food.response.PushPublicKeyResponse;
import com.br.food.response.PushTestResponse;
import com.br.food.models.PushSubscription;
import com.br.food.service.PortalAccessService;
import com.br.food.service.PushNotificationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/push")
@Tag(name = "Push notifications")
public class PushController {

	private final PushNotificationService pushNotificationService;
	private final PortalAccessService portalAccessService;

	public PushController(PushNotificationService pushNotificationService, PortalAccessService portalAccessService) {
		this.pushNotificationService = pushNotificationService;
		this.portalAccessService = portalAccessService;
	}

	@Operation(summary = "Chave pública VAPID para registro de subscription no front")
	@GetMapping("/public-key")
	public ResponseEntity<PushPublicKeyResponse> getPublicKey() {
		return ResponseEntity.ok(new PushPublicKeyResponse(pushNotificationService.getPublicKey()));
	}

	@Operation(summary = "Registra subscription do navegador (cliente ou cozinha)", description = "Tópico do cliente: o próprio cliente (X-Customer-Token) ou a equipe. Demais tópicos: so a equipe.")
	@PostMapping("/subscriptions")
	public ResponseEntity<Void> subscribe(@Valid @RequestBody PushSubscriptionRequest request,
			@RequestHeader(name = PortalAccessService.CUSTOMER_TOKEN_HEADER, required = false) String customerToken) {
		if (PushSubscription.TOPIC_CUSTOMER.equals(request.getTopic())) {
			portalAccessService.requireStaffOrCustomer(customerToken, request.getCustomerId());
		} else {
			portalAccessService.requireStaff();
		}
		pushNotificationService.saveSubscription(request);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "Remove TODAS subscriptions desse endpoint (reset completo do device)")
	@DeleteMapping("/subscriptions")
	public ResponseEntity<Void> unsubscribe(@RequestParam("endpoint") String endpoint) {
		pushNotificationService.deleteSubscription(endpoint);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "Remove apenas a topic informada para o endpoint (mantem outras topics ativas)")
	@DeleteMapping("/subscriptions/topic")
	public ResponseEntity<Void> unsubscribeTopic(
			@RequestParam("endpoint") String endpoint,
			@RequestParam("topic") String topic) {
		pushNotificationService.deleteSubscriptionTopic(endpoint, topic);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "Diagnóstico de push: VAPID, contagem por tópico e subscription deste dispositivo")
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
