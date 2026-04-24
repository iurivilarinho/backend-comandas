package com.br.food.controller;

import java.nio.file.AccessDeniedException;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.br.food.request.OrderItemRequest;
import com.br.food.request.OrderRequest;
import com.br.food.response.OrderResponse;
import com.br.food.service.OrderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@Validated
@RequestMapping("/digital-orders")
@Tag(name = "Digital menu orders", description = "Public endpoints for digital menu order creation")
public class DigitalOrderController {

	private final OrderService orderService;

	public DigitalOrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	@Operation(summary = "Create order from digital menu")
	@PostMapping
	public ResponseEntity<OrderResponse> create(@Valid @RequestBody OrderRequest request) throws AccessDeniedException {
		return ResponseEntity.status(201).body(new OrderResponse(orderService.createFromDigitalMenu(request)));
	}

	@Operation(summary = "Add items to an order from digital menu")
	@PostMapping("/{id}/items")
	public ResponseEntity<OrderResponse> addItems(
			@PathVariable Long id,
			@Valid @RequestBody List<OrderItemRequest> items) {
		return ResponseEntity.ok(new OrderResponse(orderService.addItemsFromDigitalMenu(id, items)));
	}
}
