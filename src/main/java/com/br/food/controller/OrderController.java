package com.br.food.controller;

import java.nio.file.AccessDeniedException;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.br.food.enums.Types.OrderStatus;
import com.br.food.request.CancelOrderItemRequest;
import com.br.food.request.CancelOrderRequest;
import com.br.food.request.CloseOrderRequest;
import com.br.food.request.MergeOrdersRequest;
import com.br.food.request.OrderItemRequest;
import com.br.food.request.OrderRequest;
import com.br.food.request.RequestOrderCheckoutRequest;
import com.br.food.request.SplitOrderRequest;
import com.br.food.request.TransferOrderRequest;
import com.br.food.response.OrderCheckoutResponse;
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
	public ResponseEntity<OrderResponse> create(
			@Valid @RequestBody OrderRequest request,
			@RequestHeader(name = "X-Actor", required = false) String actorName) throws AccessDeniedException {
		return ResponseEntity.status(201).body(new OrderResponse(orderService.create(request, actorName)));
	}

	@Operation(summary = "Find order by id")
	@GetMapping("/{id}")
	public ResponseEntity<OrderResponse> findById(@PathVariable Long id) {
		return ResponseEntity.ok(new OrderResponse(orderService.findById(id)));
	}

	@Operation(summary = "Search orders")
	@GetMapping
	public ResponseEntity<Page<OrderResponse>> findAll(
			@RequestParam(required = false) OrderStatus status,
			@RequestParam(required = false) String tableNumber,
			@RequestParam(required = false) String code,
			@RequestParam(required = false) Long customerId,
			Pageable pageable) {
		return ResponseEntity.ok(orderService.search(status, tableNumber, code, customerId, pageable).map(OrderResponse::new));
	}

	@Operation(summary = "Update order")
	@PutMapping("/{id}")
	public ResponseEntity<OrderResponse> update(
			@PathVariable Long id,
			@Valid @RequestBody OrderRequest request,
			@RequestHeader(name = "X-Actor", required = false) String actorName) throws AccessDeniedException {
		return ResponseEntity.ok(new OrderResponse(orderService.update(id, request, actorName)));
	}

	@Operation(summary = "Add order items")
	@PostMapping("/{id}/items")
	public ResponseEntity<OrderResponse> addItems(
			@PathVariable Long id,
			@Valid @RequestBody List<OrderItemRequest> items,
			@RequestHeader(name = "X-Actor", required = false) String actorName) {
		return ResponseEntity.ok(new OrderResponse(orderService.addItems(id, items, actorName)));
	}

	@Operation(summary = "Serve an order item")
	@PostMapping("/{orderId}/items/{itemId}/serve")
	public ResponseEntity<Void> serveItem(
			@PathVariable Long orderId,
			@PathVariable Long itemId,
			@RequestHeader(name = "X-Actor", required = false) String actorName) {
		orderService.serveItem(orderId, itemId, actorName);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "Checkout order")
	@PostMapping("/{id}/close")
	public ResponseEntity<OrderCheckoutResponse> close(
			@PathVariable Long id,
			@Valid @RequestBody CloseOrderRequest request,
			@RequestHeader(name = "X-Actor", required = false) String actorName) {
		return ResponseEntity.ok(orderService.checkout(id, request, actorName));
	}

	@Operation(summary = "Request order checkout")
	@PostMapping("/{id}/request-close")
	public ResponseEntity<OrderResponse> requestClose(
			@PathVariable Long id,
			@Valid @RequestBody RequestOrderCheckoutRequest request,
			@RequestHeader(name = "X-Actor", required = false) String actorName) {
		return ResponseEntity.ok(new OrderResponse(orderService.requestCheckout(id, request, actorName)));
	}

	@Operation(summary = "Cancel one order item")
	@PostMapping("/{orderId}/items/{itemId}/cancel")
	public ResponseEntity<Void> cancelItem(
			@PathVariable Long orderId,
			@PathVariable Long itemId,
			@Valid @RequestBody CancelOrderItemRequest request,
			@RequestHeader(name = "X-Actor", required = false) String actorName) {
		orderService.cancelItem(orderId, itemId, request.getReason(), actorName);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "Cancel order")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> cancelOrder(
			@PathVariable Long id,
			@Valid @RequestBody CancelOrderRequest request,
			@RequestHeader(name = "X-Actor", required = false) String actorName) {
		orderService.cancelOrder(id, request.getReason(), actorName);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "Reopen order")
	@PostMapping("/{id}/reopen")
	public ResponseEntity<OrderResponse> reopen(
			@PathVariable Long id,
			@RequestHeader(name = "X-Actor", required = false) String actorName) throws AccessDeniedException {
		return ResponseEntity.ok(new OrderResponse(orderService.reopen(id, actorName)));
	}

	@Operation(summary = "Transfer order to another table")
	@PostMapping("/{id}/transfer")
	public ResponseEntity<OrderResponse> transfer(
			@PathVariable Long id,
			@Valid @RequestBody TransferOrderRequest request,
			@RequestHeader(name = "X-Actor", required = false) String actorName) throws AccessDeniedException {
		return ResponseEntity.ok(new OrderResponse(orderService.transfer(id, request.getTargetTableNumber(), actorName)));
	}

	@Operation(summary = "Merge two orders")
	@PostMapping("/{id}/merge")
	public ResponseEntity<OrderResponse> merge(
			@PathVariable Long id,
			@Valid @RequestBody MergeOrdersRequest request,
			@RequestHeader(name = "X-Actor", required = false) String actorName) {
		return ResponseEntity.ok(new OrderResponse(orderService.merge(id, request.getSourceOrderId(), actorName)));
	}

	@Operation(summary = "Split order into a new order")
	@PostMapping("/{id}/split")
	public ResponseEntity<OrderResponse> split(
			@PathVariable Long id,
			@Valid @RequestBody SplitOrderRequest request,
			@RequestHeader(name = "X-Actor", required = false) String actorName) throws AccessDeniedException {
		return ResponseEntity.status(201)
				.body(new OrderResponse(orderService.split(id, request.getDestinationTableId(), request.getOrderItemIds(), actorName)));
	}
}
