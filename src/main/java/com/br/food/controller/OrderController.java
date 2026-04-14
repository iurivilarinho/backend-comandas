package com.br.food.controller;

import java.nio.file.AccessDeniedException;
import java.util.List;

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

import com.br.food.enums.Types.OrderStatus;
import com.br.food.request.CloseOrderRequest;
import com.br.food.request.OrderItemRequest;
import com.br.food.request.OrderRequest;
import com.br.food.response.OrderResponse;
import com.br.food.service.OrderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@Validated
@RequestMapping("/orders")
@Tag(name = "Orders", description = "Endpoints for order management")
public class OrderController {

	private final OrderService orderService;

	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	@Operation(summary = "Create order")
	@PostMapping
	public ResponseEntity<OrderResponse> create(@Valid @RequestBody OrderRequest request) throws AccessDeniedException {
		return ResponseEntity.status(201).body(new OrderResponse(orderService.create(request)));
	}

	@Operation(summary = "Find order by id")
	@GetMapping("/{id}")
	public ResponseEntity<OrderResponse> findById(@PathVariable Long id) {
		return ResponseEntity.ok(new OrderResponse(orderService.findById(id)));
	}

	@Operation(summary = "List orders")
	@GetMapping
	public ResponseEntity<Page<OrderResponse>> findAll(Pageable pageable) {
		return ResponseEntity.ok(orderService.findAll(pageable).map(OrderResponse::new));
	}

	@Operation(summary = "Update order")
	@PutMapping("/{id}")
	public ResponseEntity<OrderResponse> update(@PathVariable Long id, @Valid @RequestBody OrderRequest request)
			throws AccessDeniedException {
		return ResponseEntity.ok(new OrderResponse(orderService.update(id, request)));
	}

	@Operation(summary = "Add order items")
	@PostMapping("/{id}/items")
	public ResponseEntity<OrderResponse> addItems(@PathVariable Long id, @Valid @RequestBody List<OrderItemRequest> items) {
		return ResponseEntity.ok(new OrderResponse(orderService.addItems(id, items)));
	}

	@Operation(summary = "Update order status")
	@PatchMapping("/{id}/status")
	public ResponseEntity<Void> updateStatus(@PathVariable Long id, @RequestParam OrderStatus status) {
		orderService.updateStatus(id, status);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "Close order")
	@PostMapping("/{id}/close")
	public ResponseEntity<Void> close(@PathVariable Long id, @Valid @RequestBody CloseOrderRequest request) {
		orderService.closeOrder(id, request.getPaymentMethod());
		return ResponseEntity.noContent().build();
	}
}

