package com.br.food.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.AccessDeniedException;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import com.br.food.enums.Types.OrderChannel;
import com.br.food.enums.Types.OrderStatus;
import com.br.food.enums.Types.PaymentMethod;
import com.br.food.models.Order;
import com.br.food.request.OrderItemRequest;
import com.br.food.request.OrderRequest;
import com.br.food.response.OrderResponse;
import com.br.food.service.OrderService;

@ExtendWith(MockitoExtension.class)
class DigitalOrderControllerTest {

	@Mock
	private OrderService orderService;

	@InjectMocks
	private DigitalOrderController digitalOrderController;

	@Test
	void createShouldReturnCreatedWhenServiceProducesOrder() throws AccessDeniedException {
		OrderRequest request = buildDeliveryRequest();
		Order serviceOrder = buildOpenOrder(50L, "ABC123", OrderChannel.DELIVERY);

		when(orderService.createFromDigitalMenu(request)).thenReturn(serviceOrder);

		ResponseEntity<OrderResponse> response = digitalOrderController.create(request);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(50L, response.getBody().getId());
		assertEquals("ABC123", response.getBody().getCode());
		assertEquals(OrderChannel.DELIVERY, response.getBody().getChannel());
		assertEquals(OrderStatus.OPEN, response.getBody().getStatus());
		verify(orderService).createFromDigitalMenu(request);
	}

	@Test
	void createShouldPropagateAccessDeniedFromService() throws AccessDeniedException {
		OrderRequest request = buildDeliveryRequest();

		when(orderService.createFromDigitalMenu(request)).thenThrow(new AccessDeniedException("denied"));

		AccessDeniedException exception = org.junit.jupiter.api.Assertions.assertThrows(
				AccessDeniedException.class,
				() -> digitalOrderController.create(request));

		assertEquals("denied", exception.getMessage());
	}

	@Test
	void addItemsShouldReturnOkWhenServiceAcceptsItems() {
		OrderItemRequest itemRequest = buildItemRequest();
		List<OrderItemRequest> items = List.of(itemRequest);
		Order serviceOrder = buildOpenOrder(60L, "XYZ789", OrderChannel.TAKEAWAY);

		when(orderService.addItemsFromDigitalMenu(60L, items)).thenReturn(serviceOrder);

		ResponseEntity<OrderResponse> response = digitalOrderController.addItems(60L, items);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(60L, response.getBody().getId());
		assertEquals("XYZ789", response.getBody().getCode());
		assertEquals(OrderChannel.TAKEAWAY, response.getBody().getChannel());
		verify(orderService).addItemsFromDigitalMenu(eq(60L), eq(items));
	}

	private OrderRequest buildDeliveryRequest() {
		OrderRequest request = new OrderRequest();
		ReflectionTestUtils.setField(request, "customerId", 7L);
		ReflectionTestUtils.setField(request, "channel", OrderChannel.DELIVERY);
		ReflectionTestUtils.setField(request, "paymentMethod", PaymentMethod.PIX);
		ReflectionTestUtils.setField(request, "items", List.of(buildItemRequest()));
		return request;
	}

	private OrderItemRequest buildItemRequest() {
		OrderItemRequest itemRequest = new OrderItemRequest();
		ReflectionTestUtils.setField(itemRequest, "productId", 100L);
		ReflectionTestUtils.setField(itemRequest, "quantity", 1);
		return itemRequest;
	}

	private Order buildOpenOrder(Long id, String code, OrderChannel channel) {
		Order order = new Order();
		ReflectionTestUtils.setField(order, "id", id);
		order.setCode(code);
		order.setStatus(OrderStatus.OPEN);
		ReflectionTestUtils.setField(order, "channel", channel);
		return order;
	}
}
