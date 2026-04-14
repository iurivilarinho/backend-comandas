package com.br.food.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.br.food.enums.Types.OrderItemStatus;
import com.br.food.enums.Types.OrderStatus;
import com.br.food.enums.Types.PaymentMethod;
import com.br.food.models.Payment;
import com.br.food.models.OrderItem;
import com.br.food.models.DiningTable;
import com.br.food.models.Order;
import com.br.food.models.Product;
import com.br.food.repository.OrderRepository;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

	@Mock
	private OrderRepository orderRepository;

	@Mock
	private CustomerService customerService;

	@Mock
	private DiningTableService diningTableService;

	@Mock
	private EventService eventService;

	@Mock
	private ProductService productService;

	@Mock
	private PaymentService paymentService;

	@InjectMocks
	private OrderService orderService;

	@Test
	void closeOrderShouldSetPaymentReleaseTableAndCompleteOrder() {
		Order order = new Order();
		DiningTable table = new DiningTable("12");
		Product product = new Product();
		product.setDescription("Burger");
		product.setPrice(new BigDecimal("35.00"));

		OrderItem item = new OrderItem();
		item.setProduct(product);
		item.setQuantity(2);
		item.setStatus(OrderItemStatus.SERVED);

		order.setDiningTable(table);
		order.setStatus(OrderStatus.IN_PROGRESS);
		order.setDiscountPercentage(new BigDecimal("10"));
		order.getItems().add(item);

		when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
		when(paymentService.createOrReuse(PaymentMethod.PIX, new BigDecimal("63.00")))
				.thenReturn(new Payment(PaymentMethod.PIX, new BigDecimal("63.00")));

		orderService.closeOrder(1L, PaymentMethod.PIX);

		assertEquals(OrderStatus.COMPLETED, order.getStatus());
		assertEquals(new BigDecimal("63.00"), order.getTotalAmount());
		assertEquals(PaymentMethod.PIX, order.getPayment().getPaymentMethod());
		verify(diningTableService).releaseTable(null);
	}

	@Test
	void closeOrderShouldRejectWhenThereAreItemsInPreparation() {
		Order order = new Order();
		Product product = new Product();
		product.setDescription("Pizza");

		OrderItem item = new OrderItem();
		item.setProduct(product);
		item.setQuantity(1);
		item.setStatus(OrderItemStatus.IN_PREPARATION);

		order.setStatus(OrderStatus.IN_PROGRESS);
		order.getItems().add(item);

		when(orderRepository.findById(2L)).thenReturn(Optional.of(order));

		assertThrows(org.springframework.dao.DataIntegrityViolationException.class,
				() -> orderService.closeOrder(2L, PaymentMethod.CASH));
		verify(paymentService, never()).createOrReuse(PaymentMethod.CASH, BigDecimal.ZERO);
	}
}

