package com.br.food.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.br.food.request.PromotionRequest;
import com.br.food.response.PromotionResponse;
import com.br.food.service.PromotionService;

import jakarta.validation.Valid;

@RestController
@Validated
@RequestMapping("/promotions")
public class PromotionController {

	private final PromotionService promotionService;

	public PromotionController(PromotionService promotionService) {
		this.promotionService = promotionService;
	}

	@GetMapping
	public ResponseEntity<Page<PromotionResponse>> findAll(Pageable pageable,
			@RequestParam(required = false) Boolean active,
			@RequestParam(required = false) Boolean onlyValid) {
		return ResponseEntity.ok(promotionService.findAll(pageable, active, onlyValid).map(PromotionResponse::new));
	}

	@PostMapping
	public ResponseEntity<PromotionResponse> create(@Valid @RequestBody PromotionRequest request) {
		return ResponseEntity.status(201).body(new PromotionResponse(promotionService.create(request)));
	}

	@PutMapping("/{id}")
	public ResponseEntity<PromotionResponse> update(@PathVariable Long id, @Valid @RequestBody PromotionRequest request) {
		return ResponseEntity.ok(new PromotionResponse(promotionService.update(id, request)));
	}

	@PatchMapping("/{id}/status")
	public ResponseEntity<Void> updateStatus(@PathVariable Long id, @RequestParam boolean active) {
		promotionService.updateStatus(id, active);
		return ResponseEntity.noContent().build();
	}
}
