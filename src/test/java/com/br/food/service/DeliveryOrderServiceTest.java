package com.br.food.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import com.br.food.enums.Types.OrderChannel;
import com.br.food.enums.Types.OrderStatus;
import com.br.food.enums.Types.PaymentMethod;
import com.br.food.models.Address;
import com.br.food.models.CompanyProfile;
import com.br.food.models.Customer;
import com.br.food.models.Order;
import com.br.food.models.PushSubscription;
import com.br.food.repository.OrderPaymentRepository;
import com.br.food.repository.OrderRepository;
import com.br.food.request.OrderItemRequest;
import com.br.food.request.OrderRequest;
import com.br.food.request.RequestOrderCheckoutRequest;

@ExtendWith(MockitoExtension.class)
class DeliveryOrderServiceTest {

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

	@Mock
	private PushNotificationService pushNotificationService;

	@InjectMocks
	private OrderService orderService;

	@Test
	void createFromDigitalMenuShouldRejectWhenDigitalOrderingDisabled() {
		CompanyProfile companyProfile = new CompanyProfile();
		ReflectionTestUtils.setField(companyProfile, "digitalOrderingEnabled", Boolean.FALSE);

		OrderRequest request = buildDeliveryRequest(99L, PaymentMethod.PIX);

		when(companyProfileService.findCurrent()).thenReturn(companyProfile);

		DataIntegrityViolationException exception = assertThrows(
				DataIntegrityViolationException.class,
				() -> orderService.createFromDigitalMenu(request));

		assertEquals("Pedidos pelo cardapio digital estao desativados.", exception.getMessage());
	}

	@Test
	void createFromDigitalMenuShouldRejectWhenDeliveryChannelDisabled() {
		CompanyProfile companyProfile = new CompanyProfile();
		ReflectionTestUtils.setField(companyProfile, "digitalOrderingEnabled", Boolean.TRUE);
		ReflectionTestUtils.setField(companyProfile, "deliveryEnabled", Boolean.FALSE);

		OrderRequest request = buildDeliveryRequest(99L, PaymentMethod.PIX);

		when(companyProfileService.findCurrent()).thenReturn(companyProfile);

		DataIntegrityViolationException exception = assertThrows(
				DataIntegrityViolationException.class,
				() -> orderService.createFromDigitalMenu(request));

		assertEquals("Pedidos de entrega estao desativados.", exception.getMessage());
	}

	@Test
	void createFromDigitalMenuShouldRejectWhenTakeawayChannelDisabled() {
		CompanyProfile companyProfile = new CompanyProfile();
		ReflectionTestUtils.setField(companyProfile, "digitalOrderingEnabled", Boolean.TRUE);
		ReflectionTestUtils.setField(companyProfile, "takeawayEnabled", Boolean.FALSE);

		OrderRequest request = buildTakeawayRequest(99L, PaymentMethod.CARD);

		when(companyProfileService.findCurrent()).thenReturn(companyProfile);

		DataIntegrityViolationException exception = assertThrows(
				DataIntegrityViolationException.class,
				() -> orderService.createFromDigitalMenu(request));

		assertEquals("Pedidos para retirada estao desativados.", exception.getMessage());
	}

	@Test
	void createFromDigitalMenuShouldRejectDeliveryWithoutPaymentMethod() {
		CompanyProfile companyProfile = enabledCompanyProfile();
		OrderRequest request = buildDeliveryRequest(99L, null);

		when(companyProfileService.findCurrent()).thenReturn(companyProfile);

		DataIntegrityViolationException exception = assertThrows(
				DataIntegrityViolationException.class,
				() -> orderService.createFromDigitalMenu(request));

		assertEquals("Informe a forma de pagamento para pedidos de entrega ou retirada.", exception.getMessage());
	}

	@Test
	void createFromDigitalMenuShouldRejectChangeForAmountOnNonCashPayment() {
		CompanyProfile companyProfile = enabledCompanyProfile();
		OrderRequest request = buildDeliveryRequest(99L, PaymentMethod.PIX);
		ReflectionTestUtils.setField(request, "changeForAmount", new BigDecimal("50.00"));

		when(companyProfileService.findCurrent()).thenReturn(companyProfile);

		DataIntegrityViolationException exception = assertThrows(
				DataIntegrityViolationException.class,
				() -> orderService.createFromDigitalMenu(request));

		assertEquals("Troco so se aplica a pagamentos em dinheiro.", exception.getMessage());
	}

	@Test
	void createShouldRejectDeliveryWhenCustomerHasNoAddress() {
		Customer customer = buildCustomer(7L, "12345678901", null);
		OrderRequest request = buildDeliveryRequest(7L, PaymentMethod.CASH);

		when(customerService.findById(7L)).thenReturn(customer);

		DataIntegrityViolationException exception = assertThrows(
				DataIntegrityViolationException.class,
				() -> orderService.create(request, "tester"));

		assertEquals("Pedidos de entrega exigem endereco cadastrado para o cliente.", exception.getMessage());
	}

	@Test
	void createShouldRejectBlockedCustomer() {
		Customer customer = buildCustomer(7L, "12345678901", buildAddress());
		customer.setBlocked(Boolean.TRUE);

		OrderRequest request = buildDeliveryRequest(7L, PaymentMethod.PIX);

		when(customerService.findById(7L)).thenReturn(customer);

		DataIntegrityViolationException exception = assertThrows(
				DataIntegrityViolationException.class,
				() -> orderService.create(request, "tester"));

		assertEquals("Clientes bloqueados nao podem abrir novos pedidos.", exception.getMessage());
	}

	@Test
	void createShouldReturnExistingOpenOrderInsteadOfDuplicating() throws AccessDeniedException {
		Customer customer = buildCustomer(7L, "12345678901", buildAddress());
		Order existingOpenOrder = new Order();
		existingOpenOrder.setStatus(OrderStatus.OPEN);

		OrderRequest request = buildDeliveryRequest(7L, PaymentMethod.PIX);

		when(customerService.findById(7L)).thenReturn(customer);
		when(customerService.findByDocumentNumber("12345678901")).thenReturn(Optional.of(customer));
		when(orderRepository.findFirstByCustomerIdAndStatusInOrderByOpenedAtDesc(
				customer.getId(),
				List.of(OrderStatus.OPEN, OrderStatus.READY_TO_CLOSE)))
				.thenReturn(existingOpenOrder);

		Order result = orderService.create(request, "tester");

		assertSame(existingOpenOrder, result);
		verify(orderRepository, never()).save(existingOpenOrder);
		verify(auditLogService, never())
				.register(anyString(), anyLong(), anyString(), anyString(), anyString());
	}

	@Test
	void createShouldAllowTakeawayWithoutCustomerAddress() throws AccessDeniedException {
		Customer customer = buildCustomer(7L, "12345678901", null);
		OrderRequest request = buildTakeawayRequest(7L, PaymentMethod.CASH);

		Order existingOpenOrder = new Order();
		existingOpenOrder.setStatus(OrderStatus.OPEN);

		when(customerService.findById(7L)).thenReturn(customer);
		when(customerService.findByDocumentNumber("12345678901")).thenReturn(Optional.of(customer));
		when(orderRepository.findFirstByCustomerIdAndStatusInOrderByOpenedAtDesc(
				customer.getId(),
				List.of(OrderStatus.OPEN, OrderStatus.READY_TO_CLOSE)))
				.thenReturn(existingOpenOrder);

		Order result = orderService.create(request, "tester");

		assertSame(existingOpenOrder, result);
	}

	@Test
	void createShouldRejectDineInWithoutTableNumber() {
		OrderRequest request = new OrderRequest();
		ReflectionTestUtils.setField(request, "customerId", 1L);
		ReflectionTestUtils.setField(request, "channel", OrderChannel.DINE_IN);
		ReflectionTestUtils.setField(request, "items", List.of(buildItemRequest()));

		DataIntegrityViolationException exception = assertThrows(
				DataIntegrityViolationException.class,
				() -> orderService.create(request, "tester"));

		assertEquals("Informe o numero da mesa para pedidos no salao.", exception.getMessage());
	}

	@Test
	void createShouldRejectWhenOrderHasNoItems() {
		OrderRequest request = new OrderRequest();
		ReflectionTestUtils.setField(request, "customerId", 1L);
		ReflectionTestUtils.setField(request, "channel", OrderChannel.DELIVERY);
		ReflectionTestUtils.setField(request, "paymentMethod", PaymentMethod.PIX);
		ReflectionTestUtils.setField(request, "items", List.of());

		DataIntegrityViolationException exception = assertThrows(
				DataIntegrityViolationException.class,
				() -> orderService.create(request, "tester"));

		assertEquals("Pedidos precisam conter ao menos um item.", exception.getMessage());
	}

	@Test
	void addItemsFromDigitalMenuShouldRejectWhenOrderingDisabled() {
		CompanyProfile companyProfile = new CompanyProfile();
		ReflectionTestUtils.setField(companyProfile, "digitalOrderingEnabled", Boolean.FALSE);

		when(companyProfileService.findCurrent()).thenReturn(companyProfile);

		DataIntegrityViolationException exception = assertThrows(
				DataIntegrityViolationException.class,
				() -> orderService.addItemsFromDigitalMenu(1L, List.of(buildItemRequest())));

		assertEquals("Pedidos pelo cardapio digital estao desativados.", exception.getMessage());
	}

	@Test
	void requestCheckoutShouldSetReadyToCloseAndStorePaymentMethodForDelivery() {
		Order order = new Order();
		order.setStatus(OrderStatus.OPEN);
		ReflectionTestUtils.setField(order, "channel", OrderChannel.DELIVERY);
		ReflectionTestUtils.setField(order, "discountPercentage", BigDecimal.ZERO);
		ReflectionTestUtils.setField(order, "discountAmount", BigDecimal.ZERO);
		ReflectionTestUtils.setField(order, "paidAmount", BigDecimal.ZERO);
		ReflectionTestUtils.setField(order, "serviceFeeAmount", BigDecimal.ZERO);
		ReflectionTestUtils.setField(order, "coverChargeAmount", BigDecimal.ZERO);
		ReflectionTestUtils.setField(order, "applyServiceFee", Boolean.FALSE);

		Customer customer = buildCustomer(7L, "12345678901", buildAddress());
		ReflectionTestUtils.setField(order, "customer", customer);

		RequestOrderCheckoutRequest request = new RequestOrderCheckoutRequest();
		ReflectionTestUtils.setField(request, "paymentMethod", PaymentMethod.CASH);
		ReflectionTestUtils.setField(request, "notes", "Cliente leva troco para 100");

		when(orderRepository.findById(42L)).thenReturn(Optional.of(order));
		when(orderRepository.save(order)).thenReturn(order);

		Order result = orderService.requestCheckout(42L, request, "tester");

		assertEquals(OrderStatus.READY_TO_CLOSE, result.getStatus());
		assertEquals(PaymentMethod.CASH, result.getRequestedPaymentMethod());
		assertEquals("Cliente leva troco para 100", result.getCheckoutRequestNotes());
		assertNotNull(result.getCheckoutRequestedAt());
		verify(pushNotificationService).notifyTopic(
				PushSubscription.TOPIC_TABLES,
				"Conta solicitada — Delivery",
				customer.getName() + " pediu a conta no pedido #" + result.getId() + ".",
				"/admin/mesas");
	}

	@Test
	void requestCheckoutShouldRejectClosedOrder() {
		Order order = new Order();
		order.setStatus(OrderStatus.CLOSED);

		RequestOrderCheckoutRequest request = new RequestOrderCheckoutRequest();
		ReflectionTestUtils.setField(request, "paymentMethod", PaymentMethod.PIX);

		when(orderRepository.findById(42L)).thenReturn(Optional.of(order));

		DataIntegrityViolationException exception = assertThrows(
				DataIntegrityViolationException.class,
				() -> orderService.requestCheckout(42L, request, "tester"));

		assertEquals("Pedidos fechados ou cancelados nao podem solicitar a conta.", exception.getMessage());
	}

	@Test
	void requestCheckoutShouldRejectCanceledOrder() {
		Order order = new Order();
		order.setStatus(OrderStatus.CANCELED);

		RequestOrderCheckoutRequest request = new RequestOrderCheckoutRequest();

		when(orderRepository.findById(42L)).thenReturn(Optional.of(order));

		assertThrows(
				DataIntegrityViolationException.class,
				() -> orderService.requestCheckout(42L, request, "tester"));
	}

	@Test
	void findActiveOrderByCustomerDocumentNumberShouldReturnNullForBlankDocument() {
		assertNull(orderService.findActiveOrderByCustomerDocumentNumber(""));
		assertNull(orderService.findActiveOrderByCustomerDocumentNumber(null));
	}

	private OrderRequest buildDeliveryRequest(Long customerId, PaymentMethod paymentMethod) {
		OrderRequest request = new OrderRequest();
		ReflectionTestUtils.setField(request, "customerId", customerId);
		ReflectionTestUtils.setField(request, "channel", OrderChannel.DELIVERY);
		ReflectionTestUtils.setField(request, "paymentMethod", paymentMethod);
		ReflectionTestUtils.setField(request, "items", List.of(buildItemRequest()));
		return request;
	}

	private OrderRequest buildTakeawayRequest(Long customerId, PaymentMethod paymentMethod) {
		OrderRequest request = new OrderRequest();
		ReflectionTestUtils.setField(request, "customerId", customerId);
		ReflectionTestUtils.setField(request, "channel", OrderChannel.TAKEAWAY);
		ReflectionTestUtils.setField(request, "paymentMethod", paymentMethod);
		ReflectionTestUtils.setField(request, "items", List.of(buildItemRequest()));
		return request;
	}

	private OrderItemRequest buildItemRequest() {
		OrderItemRequest itemRequest = new OrderItemRequest();
		ReflectionTestUtils.setField(itemRequest, "productId", 100L);
		ReflectionTestUtils.setField(itemRequest, "quantity", 1);
		return itemRequest;
	}

	private Customer buildCustomer(Long id, String documentNumber, Address address) {
		Customer customer = new Customer();
		ReflectionTestUtils.setField(customer, "id", id);
		ReflectionTestUtils.setField(customer, "name", "Cliente Teste");
		ReflectionTestUtils.setField(customer, "documentNumber", documentNumber);
		customer.setBlocked(Boolean.FALSE);
		if (address != null) {
			customer.setAddress(address);
		}
		return customer;
	}

	private Address buildAddress() {
		Address address = new Address();
		address.setStreet("Rua das Acacias");
		address.setNumber("123");
		address.setDistrict("Centro");
		address.setPostalCode("12345-678");
		address.setCity("Sao Paulo");
		address.setStatus(Boolean.TRUE);
		return address;
	}

	private CompanyProfile enabledCompanyProfile() {
		CompanyProfile companyProfile = new CompanyProfile();
		ReflectionTestUtils.setField(companyProfile, "digitalOrderingEnabled", Boolean.TRUE);
		ReflectionTestUtils.setField(companyProfile, "deliveryEnabled", Boolean.TRUE);
		ReflectionTestUtils.setField(companyProfile, "takeawayEnabled", Boolean.TRUE);
		ReflectionTestUtils.setField(companyProfile, "dineInEnabled", Boolean.TRUE);
		return companyProfile;
	}
}
