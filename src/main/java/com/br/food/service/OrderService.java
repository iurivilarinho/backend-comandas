package com.br.food.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.br.food.enums.Types.OrderChannel;
import com.br.food.enums.Types.OrderItemStatus;
import com.br.food.enums.Types.OrderStatus;
import com.br.food.enums.Types.PaymentMethod;
import com.br.food.enums.Types.ProductType;
import com.br.food.models.Customer;
import com.br.food.models.DiningTable;
import com.br.food.models.Order;
import com.br.food.models.OrderItem;
import com.br.food.models.OrderItemIngredient;
import com.br.food.models.OrderPayment;
import com.br.food.models.Product;
import com.br.food.models.RecipeItem;
import com.br.food.repository.OrderPaymentRepository;
import com.br.food.repository.OrderRepository;
import com.br.food.repository.OrderSpecification;
import com.br.food.request.CloseOrderRequest;
import com.br.food.request.OrderItemIngredientRequest;
import com.br.food.request.OrderItemRequest;
import com.br.food.request.OrderRequest;
import com.br.food.request.PaymentLineRequest;
import com.br.food.request.RequestOrderCheckoutRequest;
import com.br.food.response.OrderCheckoutResponse;

import jakarta.persistence.EntityNotFoundException;

@Service
public class OrderService {

	private final OrderRepository orderRepository;
	private final OrderPaymentRepository orderPaymentRepository;
	private final CustomerService customerService;
	private final DiningTableService diningTableService;
	private final ProductService productService;
	private final PaymentService paymentService;
	private final RecipeService recipeService;
	private final StockEntryService stockEntryService;
	private final AuditLogService auditLogService;
	private final SystemSettingService systemSettingService;

	public OrderService(
			OrderRepository orderRepository,
			OrderPaymentRepository orderPaymentRepository,
			CustomerService customerService,
			DiningTableService diningTableService,
			ProductService productService,
			PaymentService paymentService,
			RecipeService recipeService,
			StockEntryService stockEntryService,
			AuditLogService auditLogService,
			SystemSettingService systemSettingService) {
		this.orderRepository = orderRepository;
		this.orderPaymentRepository = orderPaymentRepository;
		this.customerService = customerService;
		this.diningTableService = diningTableService;
		this.productService = productService;
		this.paymentService = paymentService;
		this.recipeService = recipeService;
		this.stockEntryService = stockEntryService;
		this.auditLogService = auditLogService;
		this.systemSettingService = systemSettingService;
	}

	@Transactional
	public Order create(OrderRequest request, String actorName) throws AccessDeniedException {
		validateOrderRequest(request);
		Customer customer = customerService.findById(request.getCustomerId());
		if (Boolean.TRUE.equals(customer.getBlocked())) {
			throw new DataIntegrityViolationException("Blocked customers cannot create new orders.");
		}
		DiningTable table = diningTableService.findByNumber(request.getTableNumber());
		Order order = new Order(request, customer, generateOrderCode(), table);
		if (request.getChannel() == OrderChannel.DINE_IN) {
			diningTableService.reserveTable(table.getId());
		}
		addItems(order, request.getItems());
		recalculateTotals(order);
		Order savedOrder = orderRepository.save(order);
		auditLogService.register("Order", savedOrder.getId(), "ORDER_CREATED", actorName, "Order code " + savedOrder.getCode());
		return savedOrder;
	}

	@Transactional
	public Order update(Long id, OrderRequest request, String actorName) throws AccessDeniedException {
		validateOrderRequest(request);
		Order order = findById(id);
		if (order.getStatus() == OrderStatus.CLOSED || order.getStatus() == OrderStatus.CANCELED) {
			throw new DataIntegrityViolationException("Closed or canceled orders cannot be updated.");
		}

		DiningTable table = diningTableService.findByNumber(request.getTableNumber());
		if (order.getDiningTable() != null && !order.getDiningTable().getId().equals(table.getId()) && order.getChannel() == OrderChannel.DINE_IN) {
			diningTableService.releaseTable(order.getDiningTable().getId());
			diningTableService.reserveTable(table.getId());
		}

		order.update(request, table);
		order.getItems().clear();
		addItems(order, request.getItems());
		recalculateTotals(order);
		auditLogService.register("Order", order.getId(), "ORDER_UPDATED", actorName, "Order items replaced.");
		return orderRepository.save(order);
	}

	@Transactional(readOnly = true)
	public Order findById(Long id) {
		return orderRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Order not found for id " + id + "."));
	}

	@Transactional(readOnly = true)
	public Page<Order> search(OrderStatus status, String tableNumber, String code, Pageable pageable) {
		Specification<Order> specification = Specification.where(OrderSpecification.hasStatus(status))
				.and(OrderSpecification.hasTableNumber(tableNumber))
				.and(OrderSpecification.hasCode(code));
		return orderRepository.findAll(specification, pageable);
	}

	@Transactional
	public Order addItems(Long id, List<OrderItemRequest> items, String actorName) {
		if (items == null || items.isEmpty()) {
			throw new DataIntegrityViolationException("At least one order item is required.");
		}
		Order order = findById(id);
		if (order.getStatus() == OrderStatus.CLOSED || order.getStatus() == OrderStatus.CANCELED) {
			throw new DataIntegrityViolationException("Cannot add items to closed or canceled orders.");
		}
		addItems(order, items);
		recalculateTotals(order);
		auditLogService.register("Order", order.getId(), "ORDER_ITEMS_ADDED", actorName, "Added " + items.size() + " items.");
		return orderRepository.save(order);
	}

	@Transactional
	public OrderCheckoutResponse checkout(Long id, CloseOrderRequest request, String actorName) {
		Order order = findById(id);
		validateOrderReadyForCheckout(order);
		applyCheckoutAdjustments(order, request);
		recalculateTotals(order);
		order.setSplitByPersonCount(request.getSplitByPersonCount());

		BigDecimal newPaidAmount = registerPayments(order, request.getPayments(), actorName);
		order.setPaidAmount(order.getPaidAmount().add(newPaidAmount));

		BigDecimal remainingAmount = order.getTotalAmount().subtract(order.getPaidAmount()).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
		BigDecimal changeAmount = calculateChange(request.getPayments(), order.getTotalAmount(), order.getPaidAmount());
		BigDecimal amountPerPerson = calculateAmountPerPerson(order);

		boolean fullyPaid = remainingAmount.compareTo(BigDecimal.ZERO) == 0;
		if (fullyPaid) {
			order.setStatus(OrderStatus.CLOSED);
			order.setClosedAt(LocalDateTime.now());
			order.setCheckoutRequestedAt(null);
			order.setRequestedPaymentMethod(null);
			order.setCheckoutRequestNotes(null);
			if (order.getDiningTable() != null && order.getChannel() == OrderChannel.DINE_IN) {
				diningTableService.releaseTable(order.getDiningTable().getId());
			}
		} else {
			order.setStatus(OrderStatus.READY_TO_CLOSE);
		}

		auditLogService.register("Order", order.getId(), "ORDER_CHECKOUT", actorName,
				"Paid=" + order.getPaidAmount() + ", remaining=" + remainingAmount + ", notes=" + sanitizeCheckoutNotes(request.getNotes()));
		return new OrderCheckoutResponse(order, remainingAmount, changeAmount, amountPerPerson, fullyPaid);
	}

	@Transactional
	public Order requestCheckout(Long id, RequestOrderCheckoutRequest request, String actorName) {
		Order order = findById(id);
		if (order.getStatus() == OrderStatus.CLOSED || order.getStatus() == OrderStatus.CANCELED) {
			throw new DataIntegrityViolationException("Closed or canceled orders cannot request checkout.");
		}

		recalculateTotals(order);
		order.setStatus(OrderStatus.READY_TO_CLOSE);
		order.setCheckoutRequestedAt(LocalDateTime.now());
		order.setRequestedPaymentMethod(request.getPaymentMethod());
		order.setCheckoutRequestNotes(request.getNotes());

		auditLogService.register(
				"Order",
				order.getId(),
				"ORDER_CHECKOUT_REQUESTED",
				actorName,
				buildCheckoutRequestDetails(order, request));
		return orderRepository.save(order);
	}

	@Transactional
	public void cancelItem(Long orderId, Long itemId, String reason, String actorName) {
		Order order = findById(orderId);
		OrderItem item = order.getItems().stream()
				.filter(orderItem -> orderItem.getId().equals(itemId))
				.findFirst()
				.orElseThrow(() -> new EntityNotFoundException("Order item not found for id " + itemId + "."));
		if (item.getStatus() == OrderItemStatus.SERVED) {
			throw new DataIntegrityViolationException("Served items cannot be canceled.");
		}
		if (item.getStatus() == OrderItemStatus.CANCELED || item.getStatus() == OrderItemStatus.DECLINED) {
			return;
		}
		if (!item.getStockConsumptions().isEmpty()) {
			stockEntryService.restoreConsumptions(item);
		}
		item.setCancellationReason(reason);
		item.setStatus(OrderItemStatus.CANCELED);
		recalculateTotals(order);
		refundRegisteredPaymentsIfNeeded(order);
		auditLogService.register("OrderItem", item.getId(), "ORDER_ITEM_CANCELED", actorName, reason);
	}

	@Transactional
	public void cancelOrder(Long orderId, String reason, String actorName) {
		Order order = findById(orderId);
		if (order.getStatus() == OrderStatus.CLOSED) {
			throw new DataIntegrityViolationException("Closed orders cannot be canceled.");
		}
		for (OrderItem item : order.getItems()) {
			if (item.getStatus() != OrderItemStatus.CANCELED && item.getStatus() != OrderItemStatus.DECLINED) {
				cancelItem(orderId, item.getId(), reason, actorName);
			}
		}
		order.setStatus(OrderStatus.CANCELED);
		if (order.getDiningTable() != null && order.getChannel() == OrderChannel.DINE_IN) {
			diningTableService.releaseTable(order.getDiningTable().getId());
		}
		auditLogService.register("Order", order.getId(), "ORDER_CANCELED", actorName, reason);
	}

	@Transactional
	public Order reopen(Long orderId, String actorName) throws AccessDeniedException {
		Order order = findById(orderId);
		if (order.getStatus() != OrderStatus.CLOSED) {
			throw new DataIntegrityViolationException("Only closed orders can be reopened.");
		}
		if (order.getDiningTable() != null && order.getChannel() == OrderChannel.DINE_IN) {
			if (!Boolean.TRUE.equals(order.getDiningTable().getActive())) {
				throw new DataIntegrityViolationException("Cannot reopen order because the original table is inactive.");
			}
			if (Boolean.TRUE.equals(order.getDiningTable().getOccupied())) {
				throw new DataIntegrityViolationException(
						"Cannot reopen order because table " + order.getDiningTable().getNumber() + " is currently occupied.");
			}
			diningTableService.reserveTable(order.getDiningTable().getId());
		}
		order.setStatus(OrderStatus.OPEN);
		order.setClosedAt(null);
		auditLogService.register("Order", order.getId(), "ORDER_REOPENED", actorName, "Order reopened.");
		return order;
	}

	@Transactional
	public Order transfer(Long orderId, String targetTableNumber, String actorName) throws AccessDeniedException {
		Order order = findById(orderId);
		if (order.getStatus() == OrderStatus.CLOSED || order.getStatus() == OrderStatus.CANCELED) {
			throw new DataIntegrityViolationException("Closed or canceled orders cannot be transferred.");
		}
		if (order.getChannel() != OrderChannel.DINE_IN) {
			throw new DataIntegrityViolationException("Only dine-in orders can be transferred between tables.");
		}
		DiningTable targetTable = diningTableService.findByNumber(targetTableNumber);
		if (order.getDiningTable() != null && order.getDiningTable().getId().equals(targetTable.getId())) {
			throw new DataIntegrityViolationException("Order is already assigned to the selected table.");
		}
		if (order.getDiningTable() != null) {
			diningTableService.releaseTable(order.getDiningTable().getId());
		}
		diningTableService.reserveTable(targetTable.getId());
		order.setDiningTable(targetTable);
		auditLogService.register("Order", order.getId(), "ORDER_TRANSFERRED", actorName, "Transferred to table " + targetTableNumber);
		return order;
	}

	@Transactional
	public Order merge(Long targetOrderId, Long sourceOrderId, String actorName) {
		Order targetOrder = findById(targetOrderId);
		Order sourceOrder = findById(sourceOrderId);
		if (targetOrder.getId().equals(sourceOrder.getId())) {
			throw new DataIntegrityViolationException("Source and target orders must be different.");
		}
		if (targetOrder.getStatus() == OrderStatus.CLOSED || targetOrder.getStatus() == OrderStatus.CANCELED
				|| sourceOrder.getStatus() == OrderStatus.CLOSED || sourceOrder.getStatus() == OrderStatus.CANCELED) {
			throw new DataIntegrityViolationException("Only active orders can be merged.");
		}
		if (targetOrder.getChannel() != sourceOrder.getChannel()) {
			throw new DataIntegrityViolationException("Orders from different channels cannot be merged.");
		}
		if (targetOrder.getChannel() == OrderChannel.DINE_IN) {
			String targetTableNumber = targetOrder.getDiningTable() != null ? targetOrder.getDiningTable().getNumber() : null;
			String sourceTableNumber = sourceOrder.getDiningTable() != null ? sourceOrder.getDiningTable().getNumber() : null;
			if (targetTableNumber == null || sourceTableNumber == null || !targetTableNumber.equals(sourceTableNumber)) {
				throw new DataIntegrityViolationException("Dine-in orders can only be merged when they belong to the same table.");
			}
		}
		for (OrderItem item : sourceOrder.getItems()) {
			item.setOrder(targetOrder);
			targetOrder.getItems().add(item);
		}
		sourceOrder.getItems().clear();
		sourceOrder.setStatus(OrderStatus.CANCELED);
		recalculateTotals(targetOrder);
		auditLogService.register("Order", targetOrder.getId(), "ORDER_MERGED", actorName, "Merged order " + sourceOrderId);
		return targetOrder;
	}

	@Transactional
	public Order split(Long orderId, Long destinationTableId, List<Long> orderItemIds, String actorName) throws AccessDeniedException {
		Order sourceOrder = findById(orderId);
		if (sourceOrder.getStatus() == OrderStatus.CLOSED || sourceOrder.getStatus() == OrderStatus.CANCELED) {
			throw new DataIntegrityViolationException("Closed or canceled orders cannot be split.");
		}
		if (sourceOrder.getChannel() != OrderChannel.DINE_IN) {
			throw new DataIntegrityViolationException("Only dine-in orders can be split between tables.");
		}
		DiningTable destinationTable = diningTableService.findById(destinationTableId);
		if (sourceOrder.getDiningTable() != null && sourceOrder.getDiningTable().getId().equals(destinationTableId)) {
			throw new DataIntegrityViolationException("Split destination must be a different table.");
		}
		diningTableService.reserveTable(destinationTableId);
		Order splitOrder = new Order();
		splitOrder.setCustomer(sourceOrder.getCustomer());
		splitOrder.setCode(generateOrderCode());
		splitOrder.setDiningTable(destinationTable);
		splitOrder.setStatus(OrderStatus.OPEN);
		splitOrder.setChannel(sourceOrder.getChannel());
		splitOrder.setDiscountPercentage(sourceOrder.getDiscountPercentage());
		splitOrder.setDiscountAmount(sourceOrder.getDiscountAmount());
		splitOrder.setOpenedAt(LocalDateTime.now());

		List<OrderItem> selectedItems = sourceOrder.getItems().stream()
				.filter(item -> orderItemIds.contains(item.getId()))
				.toList();
		if (selectedItems.isEmpty()) {
			throw new DataIntegrityViolationException("At least one order item must be selected for splitting.");
		}
		long movableItemsCount = sourceOrder.getItems().stream()
				.filter(item -> item.getStatus() != OrderItemStatus.CANCELED && item.getStatus() != OrderItemStatus.DECLINED)
				.count();
		if (selectedItems.size() >= movableItemsCount) {
			throw new DataIntegrityViolationException("Split must keep at least one item in the original order. Use transfer to move the whole order.");
		}

		sourceOrder.getItems().removeIf(item -> orderItemIds.contains(item.getId()));
		for (OrderItem item : selectedItems) {
			item.setOrder(splitOrder);
			splitOrder.getItems().add(item);
		}
		recalculateTotals(sourceOrder);
		recalculateTotals(splitOrder);
		Order savedSplitOrder = orderRepository.save(splitOrder);
		auditLogService.register("Order", savedSplitOrder.getId(), "ORDER_SPLIT", actorName, "Split from order " + orderId);
		return savedSplitOrder;
	}

	@Transactional
	public void serveItem(Long orderId, Long itemId, String actorName) {
		Order order = findById(orderId);
		OrderItem item = order.getItems().stream()
				.filter(orderItem -> orderItem.getId().equals(itemId))
				.findFirst()
				.orElseThrow(() -> new EntityNotFoundException("Order item not found for id " + itemId + "."));
		OrderItemStatus.validateTransition(item.getStatus(), OrderItemStatus.SERVED);
		item.setStatus(OrderItemStatus.SERVED);
		refreshOrderStatus(order);
		auditLogService.register("OrderItem", itemId, "ORDER_ITEM_SERVED", actorName, "Item served.");
	}

	@Transactional(readOnly = true)
	public String generateOrderCode() {
		Order highestOrder = orderRepository.findTopByOrderByCodeDesc();
		int nextCode = highestOrder != null ? Integer.parseInt(highestOrder.getCode()) + 1 : 1;
		return String.format("%04d", nextCode);
	}

	BigDecimal calculateItemsTotal(Order order) {
		return order.getItems().stream()
				.filter(item -> item.getStatus() != OrderItemStatus.CANCELED && item.getStatus() != OrderItemStatus.DECLINED)
				.map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	BigDecimal applyDiscount(BigDecimal totalAmount, BigDecimal discountPercentage, BigDecimal discountAmount) {
		BigDecimal safeDiscount = discountPercentage != null ? discountPercentage : BigDecimal.ZERO;
		BigDecimal safeDiscountAmount = discountAmount != null ? discountAmount : BigDecimal.ZERO;
		BigDecimal discountValue = totalAmount.multiply(safeDiscount).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
		return totalAmount.subtract(discountValue).subtract(safeDiscountAmount).max(BigDecimal.ZERO);
	}

	private void applyCheckoutAdjustments(Order order, CloseOrderRequest request) {
		if (request.getDiscountPercentage() != null) {
			order.setDiscountPercentage(request.getDiscountPercentage().setScale(2, RoundingMode.HALF_UP));
		}
		if (request.getDiscountAmount() != null) {
			order.setDiscountAmount(request.getDiscountAmount().setScale(2, RoundingMode.HALF_UP));
		}
	}

	private void validateOrderRequest(OrderRequest request) {
		if (request.getItems() == null || request.getItems().isEmpty()) {
			throw new DataIntegrityViolationException("Orders must contain at least one item.");
		}
		if (request.getChannel() == OrderChannel.DELIVERY && request.getTableNumber() == null) {
			throw new DataIntegrityViolationException("Table number must be informed for operational tracking.");
		}
	}

	private void addItems(Order order, List<OrderItemRequest> items) {
		for (OrderItemRequest itemRequest : items) {
			Product product = productService.findById(itemRequest.getProductId());
			validateProductForOrder(product);
			BigDecimal unitPrice = calculateCustomizedUnitPrice(product, itemRequest);
			OrderItem orderItem = new OrderItem(order, product, itemRequest, unitPrice);
			applyCustomizedIngredients(orderItem, product, itemRequest);
			order.getItems().add(orderItem);
		}
		refreshOrderStatus(order);
	}

	private void validateProductForOrder(Product product) {
		if (!Boolean.TRUE.equals(product.getActive())) {
			throw new DataIntegrityViolationException("Inactive products cannot be ordered.");
		}
		if (Boolean.TRUE.equals(product.getComplement())) {
			throw new DataIntegrityViolationException("Complement products cannot be ordered as standalone items.");
		}
	}

	private BigDecimal calculateCustomizedUnitPrice(Product product, OrderItemRequest itemRequest) {
		BigDecimal unitPrice = product.getPrice().setScale(2, RoundingMode.HALF_UP);
		Map<Long, RecipeItem> recipeItemsByIngredientId = buildRecipeItemsMap(product);

		for (OrderItemIngredientRequest ingredientRequest : itemRequest.getIngredients()) {
			Product ingredientProduct = productService.findById(ingredientRequest.getIngredientProductId());
			if (ingredientProduct.getType() != ProductType.INGREDIENT) {
				throw new DataIntegrityViolationException("Customized ingredients must use products of type INGREDIENT.");
			}

			BigDecimal baseQuantity = recipeItemsByIngredientId.containsKey(ingredientProduct.getId())
					? recipeItemsByIngredientId.get(ingredientProduct.getId()).getQuantity()
					: BigDecimal.ZERO;
			BigDecimal additionalQuantity = ingredientRequest.getQuantity().subtract(baseQuantity).max(BigDecimal.ZERO);

			unitPrice = unitPrice.add(ingredientProduct.getPrice().multiply(additionalQuantity));
		}

		return unitPrice.setScale(2, RoundingMode.HALF_UP);
	}

	private void applyCustomizedIngredients(OrderItem orderItem, Product product, OrderItemRequest itemRequest) {
		Map<Long, RecipeItem> recipeItemsByIngredientId = buildRecipeItemsMap(product);

		for (OrderItemIngredientRequest ingredientRequest : itemRequest.getIngredients()) {
			Product ingredientProduct = productService.findById(ingredientRequest.getIngredientProductId());
			BigDecimal baseQuantity = recipeItemsByIngredientId.containsKey(ingredientProduct.getId())
					? recipeItemsByIngredientId.get(ingredientProduct.getId()).getQuantity()
					: BigDecimal.ZERO;

			orderItem.getIngredients().add(new OrderItemIngredient(orderItem, ingredientProduct, ingredientRequest.getQuantity(), baseQuantity));
		}
	}

	private Map<Long, RecipeItem> buildRecipeItemsMap(Product product) {
		Map<Long, RecipeItem> recipeItemsByIngredientId = new LinkedHashMap<>();

		for (RecipeItem recipeItem : recipeService.findByProductId(product.getId())) {
			recipeItemsByIngredientId.put(recipeItem.getIngredientProduct().getId(), recipeItem);
		}

		return recipeItemsByIngredientId;
	}

	private void validateOrderReadyForCheckout(Order order) {
		if (order.getItems().isEmpty()) {
			throw new DataIntegrityViolationException("Orders cannot be checked out without items.");
		}
		boolean hasOpenKitchenItem = order.getItems().stream()
				.filter(item -> item.getStatus() != OrderItemStatus.CANCELED && item.getStatus() != OrderItemStatus.DECLINED)
				.filter(item -> item.getProduct() != null && Boolean.TRUE.equals(item.getProduct().getSendToKitchen()))
				.anyMatch(item -> item.getStatus() != OrderItemStatus.READY && item.getStatus() != OrderItemStatus.SERVED);
		if (hasOpenKitchenItem) {
			throw new DataIntegrityViolationException("There are still items pending in the kitchen workflow.");
		}
	}

	private BigDecimal registerPayments(Order order, List<PaymentLineRequest> paymentLines, String actorName) {
		BigDecimal totalPaidNow = BigDecimal.ZERO;
		for (PaymentLineRequest paymentLine : paymentLines) {
			paymentService.findByPaymentMethod(paymentLine.getPaymentMethod());
			validateCashPayment(paymentLine);
			OrderPayment orderPayment = new OrderPayment(
					order,
					paymentLine.getPaymentMethod(),
					paymentLine.getAmount().setScale(2, RoundingMode.HALF_UP),
					paymentLine.getCashReceived(),
					safeActor(actorName));
			order.getPayments().add(orderPaymentRepository.save(orderPayment));
			totalPaidNow = totalPaidNow.add(orderPayment.getAmount());
		}
		return totalPaidNow.setScale(2, RoundingMode.HALF_UP);
	}

	private void validateCashPayment(PaymentLineRequest paymentLine) {
		if (paymentLine.getPaymentMethod() != PaymentMethod.CASH) {
			return;
		}
		if (paymentLine.getCashReceived() == null || paymentLine.getCashReceived().compareTo(paymentLine.getAmount()) < 0) {
			throw new DataIntegrityViolationException("Cash received must be greater than or equal to the cash payment amount.");
		}
	}

	private BigDecimal calculateChange(List<PaymentLineRequest> paymentLines, BigDecimal totalAmount, BigDecimal totalPaid) {
		BigDecimal cashReceived = paymentLines.stream()
				.filter(paymentLine -> paymentLine.getPaymentMethod() == PaymentMethod.CASH && paymentLine.getCashReceived() != null)
				.map(PaymentLineRequest::getCashReceived)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal cashPaid = paymentLines.stream()
				.filter(paymentLine -> paymentLine.getPaymentMethod() == PaymentMethod.CASH)
				.map(PaymentLineRequest::getAmount)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		if (cashReceived.compareTo(BigDecimal.ZERO) == 0) {
			return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
		}
		BigDecimal cashExcess = cashReceived.subtract(cashPaid);
		return cashExcess.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
	}

	private BigDecimal calculateAmountPerPerson(Order order) {
		if (order.getSplitByPersonCount() == null || order.getSplitByPersonCount() <= 0) {
			return null;
		}
		return order.getTotalAmount().divide(BigDecimal.valueOf(order.getSplitByPersonCount()), 2, RoundingMode.HALF_UP);
	}

	private void refundRegisteredPaymentsIfNeeded(Order order) {
		if (order.getPaidAmount().compareTo(BigDecimal.ZERO) <= 0) {
			return;
		}
		recalculateTotals(order);
		if (order.getPaidAmount().compareTo(order.getTotalAmount()) > 0) {
			order.setPaidAmount(order.getTotalAmount());
		}
	}

	private void recalculateTotals(Order order) {
		BigDecimal subtotal = applyDiscount(
				calculateItemsTotal(order),
				order.getDiscountPercentage(),
				order.getDiscountAmount()).setScale(2, RoundingMode.HALF_UP);
		BigDecimal serviceFeeAmount = calculateServiceFee(order, subtotal);
		BigDecimal coverChargeAmount = calculateCoverCharge(order);
		order.setSubtotalAmount(subtotal);
		order.setServiceFeeAmount(serviceFeeAmount);
		order.setCoverChargeAmount(coverChargeAmount);
		order.setTotalAmount(subtotal.add(serviceFeeAmount).add(coverChargeAmount).setScale(2, RoundingMode.HALF_UP));
		refreshOrderStatus(order);
	}

	private BigDecimal calculateServiceFee(Order order, BigDecimal subtotal) {
		if (order.getChannel() != OrderChannel.DINE_IN) {
			return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
		}
		BigDecimal serviceFeePercent = systemSettingService.getDecimal(SystemSettingService.SERVICE_FEE_PERCENT, BigDecimal.ZERO);
		return subtotal.multiply(serviceFeePercent).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
	}

	private BigDecimal calculateCoverCharge(Order order) {
		return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
	}

	private void refreshOrderStatus(Order order) {
		if (order.getStatus() == OrderStatus.CLOSED || order.getStatus() == OrderStatus.CANCELED) {
			return;
		}
		boolean allCompleted = order.getItems().stream()
				.filter(item -> item.getStatus() != OrderItemStatus.CANCELED && item.getStatus() != OrderItemStatus.DECLINED)
				.allMatch(item -> item.getStatus() == OrderItemStatus.READY || item.getStatus() == OrderItemStatus.SERVED);
		order.setStatus(allCompleted ? OrderStatus.READY_TO_CLOSE : OrderStatus.OPEN);
	}

	private String safeActor(String actorName) {
		return actorName == null || actorName.isBlank() ? "system" : actorName.trim();
	}

	private String buildCheckoutRequestDetails(Order order, RequestOrderCheckoutRequest request) {
		String itemsSummary = order.getItems().stream()
				.filter(item -> item.getStatus() != OrderItemStatus.CANCELED && item.getStatus() != OrderItemStatus.DECLINED)
				.map(item -> item.getQuantity() + "x " + item.getProduct().getDescription() + " (" + item.getUnitPrice() + ")")
				.reduce((left, right) -> left + "; " + right)
				.orElse("No active items.");

		String requestedPaymentMethod = request.getPaymentMethod() != null ? request.getPaymentMethod().name() : "NOT_INFORMED";
		String notes = request.getNotes() != null && !request.getNotes().isBlank() ? request.getNotes().trim() : "NO_NOTES";

		return "Table=" + (order.getDiningTable() != null ? order.getDiningTable().getNumber() : "-")
				+ ", total=" + order.getTotalAmount()
				+ ", paymentMethod=" + requestedPaymentMethod
				+ ", notes=" + notes
				+ ", items=" + itemsSummary;
	}

	private String sanitizeCheckoutNotes(String notes) {
		return notes != null && !notes.isBlank() ? notes.trim() : "NO_NOTES";
	}

	@Transactional
	public void consumeRecipeForItem(OrderItem orderItem) {
		if (orderItem.getProduct() == null || orderItem.getProduct().getType() != ProductType.FINISHED) {
			return;
		}
		if (!Boolean.TRUE.equals(orderItem.getProduct().getRequiresPreparation())) {
			return;
		}
		if (!orderItem.getStockConsumptions().isEmpty()) {
			return;
		}
		if (!orderItem.getIngredients().isEmpty()) {
			for (OrderItemIngredient ingredient : orderItem.getIngredients()) {
				BigDecimal totalIngredientQuantity = ingredient.getQuantity().multiply(BigDecimal.valueOf(orderItem.getQuantity()));
				orderItem.getStockConsumptions().addAll(
						stockEntryService.decreaseStockForProduct(ingredient.getIngredientProduct().getId(), totalIngredientQuantity, orderItem));
			}
			return;
		}

		List<RecipeItem> recipeItems = recipeService.findByProductId(orderItem.getProduct().getId());
		for (RecipeItem recipeItem : recipeItems) {
			BigDecimal totalIngredientQuantity = recipeItem.getQuantity().multiply(BigDecimal.valueOf(orderItem.getQuantity()));
			orderItem.getStockConsumptions().addAll(
					stockEntryService.decreaseStockForProduct(recipeItem.getIngredientProduct().getId(), totalIngredientQuantity, orderItem));
		}
	}
}
