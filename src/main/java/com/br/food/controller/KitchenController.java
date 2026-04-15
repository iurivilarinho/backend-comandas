package com.br.food.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.br.food.request.KitchenDeclineRequest;
import com.br.food.response.PendingOrderResponse;
import com.br.food.service.KitchenService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@Validated
@RequestMapping("/kitchen")
@Tag(name = "Kitchen", description = "Endpoints for kitchen workflow operations")
public class KitchenController {

	private final KitchenService kitchenService;

	public KitchenController(KitchenService kitchenService) {
		this.kitchenService = kitchenService;
	}

	@Operation(summary = "List pending kitchen items")
	@GetMapping("/pending")
	public ResponseEntity<List<PendingOrderResponse>> pending() {
		return ResponseEntity.ok(kitchenService.listPendingItems());
	}

	@Operation(summary = "Accept order item into kitchen queue")
	@PostMapping("/accept/{itemId}")
	public ResponseEntity<Void> accept(
			@PathVariable Long itemId,
			@RequestHeader(name = "X-Actor", required = false) String actorName) {
		kitchenService.acceptOrderItem(itemId, actorName);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "Start item preparation")
	@PostMapping("/start-preparation/{itemId}")
	public ResponseEntity<Void> startPreparation(
			@PathVariable Long itemId,
			@RequestHeader(name = "X-Actor", required = false) String actorName) {
		kitchenService.startPreparation(itemId, actorName);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "Mark kitchen item as ready")
	@PostMapping("/mark-ready/{itemId}")
	public ResponseEntity<Void> markReady(
			@PathVariable Long itemId,
			@RequestHeader(name = "X-Actor", required = false) String actorName) {
		kitchenService.markReady(itemId, actorName);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "Decline kitchen item")
	@PostMapping("/decline/{itemId}")
	public ResponseEntity<Void> decline(
			@PathVariable Long itemId,
			@Valid @RequestBody KitchenDeclineRequest request,
			@RequestHeader(name = "X-Actor", required = false) String actorName) {
		kitchenService.rejectOrderItem(itemId, request.getReason(), actorName);
		return ResponseEntity.noContent().build();
	}
}
