package com.br.food.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.br.food.enums.Types.OrderItemStatus;
import com.br.food.enums.Types.OrderStatus;
import com.br.food.enums.Types.PaymentMethod;
import com.br.food.models.Order;
import com.br.food.models.OrderItem;
import com.br.food.models.OrderPayment;
import com.br.food.models.Product;
import com.br.food.models.RecipeItem;
import com.br.food.repository.OrderPaymentRepository;
import com.br.food.repository.OrderRepository;
import com.br.food.request.CloseOrderRequest;
import com.br.food.request.OrderItemIngredientRequest;
import com.br.food.request.OrderItemRequest;
import com.br.food.request.PaymentLineRequest;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

	@Mock
	private OrderRepository orderRepository;

	@Mock
	private OrderPaymentRepository orderPaymentRepository;

	@Mock
	private CustomerService customerService;

	@Mock
	private DiningTableService diningTableService;

	@Mock
	private ProductService productService;

	@Mock
	private PaymentService paymentService;

	@Mock
	private RecipeService recipeService;

	@Mock
	private StockEntryService stockEntryService;

	@Mock
	private AuditLogService auditLogService;

	@Mock
	private SystemSettingService systemSettingService;

	@Mock
	private PromotionService promotionService;

	@Mock
	private CompanyProfileService companyProfileService;

	@InjectMocks
	private OrderService orderService;

	@Test
	void calculateItemsTotalShouldIgnoreCanceledItems() {
		Order order = new Order();

		Product activeProduct = new Product();
		activeProduct.setPrice(new BigDecimal("35.00"));
		OrderItem activeItem = new OrderItem();
		activeItem.setProduct(activeProduct);
		activeItem.setUnitPrice(new BigDecimal("35.00"));
		activeItem.setQuantity(2);
		activeItem.setStatus(OrderItemStatus.READY);

		Product canceledProduct = new Product();
		canceledProduct.setPrice(new BigDecimal("50.00"));
		OrderItem canceledItem = new OrderItem();
		canceledItem.setProduct(canceledProduct);
		canceledItem.setUnitPrice(new BigDecimal("50.00"));
		canceledItem.setQuantity(1);
		canceledItem.setStatus(OrderItemStatus.CANCELED);

		order.getItems().add(activeItem);
		order.getItems().add(canceledItem);

		assertEquals(new BigDecimal("70.00"), orderService.calculateItemsTotal(order));
	}

	@Test
	void cancelOrderShouldRejectClosedOrder() {
		Order order = new Order();
		order.setStatus(OrderStatus.CLOSED);

		when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

		assertThrows(org.springframework.dao.DataIntegrityViolationException.class,
				() -> orderService.cancelOrder(1L, "customer request", "tester"));
	}

	@Test
	void addItemsShouldIncreaseUnitPriceWhenIngredientQuantityExceedsRecipe() {
		Order order = new Order();
		order.setStatus(OrderStatus.OPEN);

		Product product = new Product();
		ReflectionTestUtils.setField(product, "id", 10L);
		product.setPrice(new BigDecimal("20.00"));
		product.setActive(true);
		product.setComplement(false);

		Product ingredientProduct = new Product();
		ReflectionTestUtils.setField(ingredientProduct, "id", 20L);
		ingredientProduct.setPrice(new BigDecimal("2.50"));
		ingredientProduct.setType(com.br.food.enums.Types.ProductType.INGREDIENT);

		RecipeItem recipeItem = new RecipeItem(product, ingredientProduct, new BigDecimal("1.00"));

		OrderItemRequest itemRequest = new OrderItemRequest();
		ReflectionTestUtils.setField(itemRequest, "productId", 10L);
		ReflectionTestUtils.setField(itemRequest, "quantity", 2);

		OrderItemIngredientRequest ingredientRequest = new OrderItemIngredientRequest();
		ReflectionTestUtils.setField(ingredientRequest, "ingredientProductId", 20L);
		ReflectionTestUtils.setField(ingredientRequest, "quantity", new BigDecimal("3.00"));
		ReflectionTestUtils.setField(itemRequest, "ingredients", List.of(ingredientRequest));

		when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
		when(productService.findById(10L)).thenReturn(product);
		when(productService.findById(20L)).thenReturn(ingredientProduct);
		when(recipeService.findByProductId(10L)).thenReturn(List.of(recipeItem));
		when(orderRepository.save(order)).thenReturn(order);

		Order updatedOrder = orderService.addItems(1L, List.of(itemRequest), "tester");

		assertEquals(1, updatedOrder.getItems().size());
		assertEquals(new BigDecimal("25.00"), updatedOrder.getItems().get(0).getUnitPrice());
		assertEquals(1, updatedOrder.getItems().get(0).getIngredients().size());
	}

	@Test
	void checkoutShouldApplyDiscountAmountBeforeRegisteringPayment() {
		Order order = new Order();
		order.setStatus(OrderStatus.READY_TO_CLOSE);
		order.setDiscountPercentage(BigDecimal.ZERO);
		order.setDiscountAmount(BigDecimal.ZERO);
		order.setPaidAmount(BigDecimal.ZERO);
		order.setServiceFeeAmount(BigDecimal.ZERO);
		order.setCoverChargeAmount(BigDecimal.ZERO);
		order.setPayments(new ArrayList<>());

		Product product = new Product();
		product.setPrice(new BigDecimal("40.00"));
		OrderItem item = new OrderItem();
		item.setProduct(product);
		item.setUnitPrice(new BigDecimal("40.00"));
		item.setQuantity(2);
		item.setStatus(OrderItemStatus.READY);
		order.getItems().add(item);

		PaymentLineRequest paymentLine = new PaymentLineRequest();
		ReflectionTestUtils.setField(paymentLine, "paymentMethod", PaymentMethod.PIX);
		ReflectionTestUtils.setField(paymentLine, "amount", new BigDecimal("70.00"));

		CloseOrderRequest request = new CloseOrderRequest();
		ReflectionTestUtils.setField(request, "payments", List.of(paymentLine));
		ReflectionTestUtils.setField(request, "discountAmount", new BigDecimal("10.00"));

		when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
		when(orderPaymentRepository.save(any(OrderPayment.class))).thenAnswer(invocation -> invocation.getArgument(0));

		orderService.checkout(1L, request, "tester");

		assertEquals(new BigDecimal("10.00"), order.getDiscountAmount());
		assertEquals(new BigDecimal("70.00"), order.getSubtotalAmount());
		assertEquals(new BigDecimal("70.00"), order.getTotalAmount());
		assertEquals(OrderStatus.CLOSED, order.getStatus());
		verify(paymentService).findByPaymentMethod(PaymentMethod.PIX);
	}
}
