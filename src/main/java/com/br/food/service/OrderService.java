package com.br.food.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.br.food.enums.Types.OrderChannel;
import com.br.food.enums.Types.OrderItemStatus;
import com.br.food.enums.Types.OrderStatus;
import com.br.food.enums.Types.PaymentMethod;
import com.br.food.models.Customer;
import com.br.food.models.Event;
import com.br.food.models.Payment;
import com.br.food.models.DiningTable;
import com.br.food.models.Order;
import com.br.food.models.OrderItem;
import com.br.food.models.Product;
import com.br.food.repository.OrderRepository;
import com.br.food.request.OrderItemRequest;
import com.br.food.request.OrderRequest;

import jakarta.persistence.EntityNotFoundException;

@Service
public class OrderService {

	private final OrderRepository orderRepository;
	private final CustomerService customerService;
	private final DiningTableService diningTableService;
	private final EventService eventService;
	private final ProductService productService;
	private final PaymentService paymentService;

	public OrderService(
			OrderRepository orderRepository,
			CustomerService customerService,
			DiningTableService diningTableService,
			EventService eventService,
			ProductService productService,
			PaymentService paymentService) {
		this.orderRepository = orderRepository;
		this.customerService = customerService;
		this.diningTableService = diningTableService;
		this.eventService = eventService;
		this.productService = productService;
		this.paymentService = paymentService;
	}

	@Transactional
	public Order create(OrderRequest request) throws AccessDeniedException {
		Customer customer = customerService.findById(request.getCustomerId());
		DiningTable table = diningTableService.findByNumber(request.getTableNumber());
		Order order = new Order(request, customer, generateOrderCode(), table);
		diningTableService.reserveTable(table.getId());
		addEventChargeIfNeeded(order);
		addItems(order, request.getItems());
		order.setTotalAmount(calculateDiscountedTotal(order));
		return orderRepository.save(order);
	}

	@Transactional
	public Order update(Long id, OrderRequest request) throws AccessDeniedException {
		Order order = findById(id);
		DiningTable table = diningTableService.findByNumber(request.getTableNumber());
		Payment paymentMethod = request.getPaymentMethod() != null
				? paymentService.findByPaymentMethod(request.getPaymentMethod())
				: null;

		if (!order.getDiningTable().getId().equals(table.getId())) {
			diningTableService.releaseTable(order.getDiningTable().getId());
			diningTableService.reserveTable(table.getId());
		}

		order.update(request, table, paymentMethod);
		order.getItems().clear();
		addEventChargeIfNeeded(order);
		addItems(order, request.getItems());
		order.setTotalAmount(calculateDiscountedTotal(order));
		return orderRepository.save(order);
	}

	@Transactional(readOnly = true)
	public Order findById(Long id) {
		return orderRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Order not found for id " + id + "."));
	}

	@Transactional(readOnly = true)
	public Page<Order> findAll(Pageable pageable) {
		return orderRepository.findAll(pageable);
	}

	@Transactional
	public void updateStatus(Long id, OrderStatus targetStatus) {
		Order order = findById(id);
		OrderStatus.validateTransition(order.getStatus(), targetStatus);
		order.setStatus(targetStatus);
	}

	@Transactional
	public Order addItems(Long id, List<OrderItemRequest> items) {
		Order order = findById(id);
		addItems(order, items);
		order.setTotalAmount(calculateDiscountedTotal(order));
		return orderRepository.save(order);
	}

	@Transactional
	public void closeOrder(Long id, PaymentMethod paymentMethod) {
		Order order = findById(id);
		validateNoItemsInPreparation(order);
		OrderStatus.validateTransition(order.getStatus(), OrderStatus.COMPLETED);
		order.setStatus(OrderStatus.COMPLETED);
		order.setClosedAt(LocalDateTime.now());

		BigDecimal totalAmount = calculateDiscountedTotal(order);
		order.setTotalAmount(totalAmount);
		order.setPayment(paymentService.createOrReuse(paymentMethod, totalAmount));
		diningTableService.releaseTable(order.getDiningTable().getId());
	}

	@Transactional(readOnly = true)
	public String generateOrderCode() {
		Order highestOrder = orderRepository.findTopByOrderByCodeDesc();
		int nextCode = highestOrder != null ? Integer.parseInt(highestOrder.getCode()) + 1 : 1;
		return String.format("%04d", nextCode);
	}

	BigDecimal calculateItemsTotal(Order order) {
		return order.getItems().stream().map(item -> {
			BigDecimal unitPrice = item.getProduct() != null ? item.getProduct().getPrice()
					: item.getEvent() != null ? item.getEvent().getValue() : BigDecimal.ZERO;
			return unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
		}).reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	BigDecimal applyDiscount(BigDecimal totalAmount, BigDecimal discountPercentage) {
		BigDecimal safeDiscount = discountPercentage != null ? discountPercentage : BigDecimal.ZERO;
		BigDecimal discountValue = totalAmount.multiply(safeDiscount)
				.divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
		return totalAmount.subtract(discountValue);
	}

	private BigDecimal calculateDiscountedTotal(Order order) {
		return applyDiscount(calculateItemsTotal(order), order.getDiscountPercentage());
	}

	private void validateNoItemsInPreparation(Order order) {
		List<String> itemsInPreparation = order.getItems().stream()
				.filter(item -> item.getStatus() == OrderItemStatus.IN_PREPARATION)
				.map(item -> item.getProduct() != null ? item.getProduct().getDescription() : "Event charge")
				.toList();

		if (!itemsInPreparation.isEmpty()) {
			throw new DataIntegrityViolationException(
					"These items are still in preparation: " + String.join(", ", itemsInPreparation));
		}
	}

	private void addEventChargeIfNeeded(Order order) {
		if (order.getChannel() != OrderChannel.DINE_IN) {
			return;
		}
		Event event = eventService.findOpenEvent();
		if (event != null) {
			order.getItems().add(new OrderItem(order, event, 1));
		}
	}

	private void addItems(Order order, List<OrderItemRequest> items) {
		for (OrderItemRequest itemRequest : items) {
			Product product = productService.findById(itemRequest.getProductId());
			order.getItems().add(new OrderItem(order, product, itemRequest));
		}
		if (order.getStatus() == OrderStatus.PENDING_APPROVAL && !order.getItems().isEmpty()) {
			order.setStatus(OrderStatus.IN_PROGRESS);
		}
	}
}
