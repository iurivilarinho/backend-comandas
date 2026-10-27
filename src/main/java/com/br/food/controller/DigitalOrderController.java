package com.br.food.controller;

import java.nio.file.AccessDeniedException;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.br.food.request.OrderItemRequest;
import com.br.food.request.OrderRequest;
import com.br.food.response.OrderResponse;
import com.br.food.models.Order;
import com.br.food.service.OrderService;
import com.br.food.service.PortalAccessService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@Validated
@RequestMapping("/digital-orders")
@Tag(name = "Digital menu orders", description = "Public endpoints for digital menu order creation")
public class DigitalOrderController {

	private final OrderService orderService;
	private final PortalAccessService portalAccessService;

	public DigitalOrderController(OrderService orderService, PortalAccessService portalAccessService) {
		this.orderService = orderService;
		this.portalAccessService = portalAccessService;
	}

	@Operation(summary = "Create order from digital menu")
	@PostMapping
	public ResponseEntity<OrderResponse> create(@Valid @RequestBody OrderRequest request,
			@RequestHeader(name = PortalAccessService.CUSTOMER_TOKEN_HEADER, required = false) String customerToken)
			throws AccessDeniedException {
		portalAccessService.requireStaffOrCustomer(customerToken, request.getCustomerId());
		return ResponseEntity.status(201).body(new OrderResponse(orderService.createFromDigitalMenu(request)));
	}

	@Operation(summary = "Add items to an order from digital menu")
	@PostMapping("/{id}/items")
	public ResponseEntity<OrderResponse> addItems(
			@PathVariable Long id,
			@Valid @RequestBody List<OrderItemRequest> items,
			@RequestHeader(name = PortalAccessService.CUSTOMER_TOKEN_HEADER, required = false) String customerToken) {
		portalAccessService.loadOwned(customerToken, () -> orderService.findById(id),
				order -> order.getCustomer() != null ? order.getCustomer().getId() : null);
		return ResponseEntity.ok(new OrderResponse(orderService.addItemsFromDigitalMenu(id, items)));
	}
}
