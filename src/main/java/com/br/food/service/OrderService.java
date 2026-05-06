package com.br.food.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.br.food.enums.Types.OrderChannel;
import com.br.food.enums.Types.OrderItemStatus;
import com.br.food.enums.Types.OrderStatus;
import com.br.food.enums.Types.PaymentMethod;
import com.br.food.enums.Types.ProductType;
import com.br.food.models.CompanyProfile;
import com.br.food.models.Customer;
import com.br.food.models.DiningTable;
import com.br.food.models.Order;
import com.br.food.models.OrderItem;
import com.br.food.models.OrderItemIngredient;
import com.br.food.models.OrderItemVariation;
import com.br.food.models.OrderPayment;
import com.br.food.models.Product;
import com.br.food.models.ProductVariation;
import com.br.food.models.ProductVariationGroup;
import com.br.food.models.Promotion;
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
	private final PromotionService promotionService;
	private final CompanyProfileService companyProfileService;
	private final PushNotificationService pushNotificationService;

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
			SystemSettingService systemSettingService,
			PromotionService promotionService,
			CompanyProfileService companyProfileService,
			PushNotificationService pushNotificationService) {
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
		this.promotionService = promotionService;
		this.companyProfileService = companyProfileService;
		this.pushNotificationService = pushNotificationService;
	}

	private void notifyKitchenOfNewItems(Order order) {
		long pendingForKitchen = order.getItems().stream()
				.filter(item -> item.getStatus() == OrderItemStatus.RECEIVED)
				.filter(item -> item.getProduct() != null && Boolean.TRUE.equals(item.getProduct().getSendToKitchen()))
				.count();
		if (pendingForKitchen <= 0) {
			return;
		}
		String code = order.getCode() != null ? order.getCode() : ("#" + order.getId());
		pushNotificationService.notifyTopic(
				com.br.food.models.PushSubscription.TOPIC_KITCHEN,
				"Novo pedido para a cozinha",
				code + " — " + describeOrderOrigin(order) + " enviou " + pendingForKitchen
						+ (pendingForKitchen == 1 ? " item." : " itens."),
				"/admin/cozinha");
	}

	private void notifyDeliveryOfNewOrder(Order order) {
		if (order.getChannel() == OrderChannel.DINE_IN) {
			return;
		}
		String code = order.getCode() != null ? order.getCode() : ("#" + order.getId());
		String customerName = order.getCustomer() != null && order.getCustomer().getName() != null
				? order.getCustomer().getName()
				: "Cliente";
		long itemCount = order.getItems().stream()
				.filter(item -> item.getStatus() != OrderItemStatus.CANCELED && item.getStatus() != OrderItemStatus.DECLINED)
				.mapToLong(item -> item.getQuantity() != null ? item.getQuantity() : 0L)
				.sum();
		pushNotificationService.notifyTopic(
				com.br.food.models.PushSubscription.TOPIC_DELIVERY,
				"Novo pedido " + describeOrderOrigin(order),
				code + " — " + customerName + " (" + itemCount + (itemCount == 1 ? " item)" : " itens)"),
				"/admin/delivery");
	}

	private String describeOrderOrigin(Order order) {
		if (order.getChannel() == OrderChannel.DELIVERY) {
			return "Delivery";
		}
		if (order.getChannel() == OrderChannel.TAKEAWAY) {
			return "Retirada";
		}
		if (order.getDiningTable() != null && order.getDiningTable().getNumber() != null) {
			return "Mesa " + order.getDiningTable().getNumber();
		}
		return "Pedido digital";
	}

	@Transactional
	public Order createFromDigitalMenu(OrderRequest request) throws AccessDeniedException {
		validateDigitalOrderingEnabled();
		validateChannelEnabled(request.getChannel());
		return create(request, null);
	}

	@Transactional
	public Order create(OrderRequest request, String actorName) throws AccessDeniedException {
		validateOrderRequest(request);
		Customer customer = customerService.findById(request.getCustomerId());
		if (Boolean.TRUE.equals(customer.getBlocked())) {
			throw new DataIntegrityViolationException("Clientes bloqueados nao podem abrir novos pedidos.");
		}
		if (request.getChannel() == OrderChannel.DELIVERY && customer.getAddress() == null) {
			throw new DataIntegrityViolationException("Pedidos de entrega exigem endereco cadastrado para o cliente.");
		}
		Order existingOpenOrder = findActiveOrderByCustomerDocumentNumber(customer.getDocumentNumber());
		if (existingOpenOrder != null) {
			return existingOpenOrder;
		}
		DiningTable table = request.getChannel() == OrderChannel.DINE_IN
				? diningTableService.findByNumber(request.getTableNumber())
				: null;
		Order order = new Order(request, customer, generateOrderCode(), table);
		if (request.getChannel() == OrderChannel.DINE_IN) {
			diningTableService.reserveTable(table.getId());
		}
		addItems(order, request.getItems());
		recalculateTotals(order);
		Order savedOrder = orderRepository.save(order);
		auditLogService.register("Order", savedOrder.getId(), "ORDER_CREATED", actorName, "Order code " + savedOrder.getCode());
		notifyKitchenOfNewItems(savedOrder);
		notifyDeliveryOfNewOrder(savedOrder);
		return savedOrder;
	}

	@Transactional
	public Order update(Long id, OrderRequest request, String actorName) throws AccessDeniedException {
		validateOrderRequest(request);
		Order order = findById(id);
		if (order.getStatus() == OrderStatus.CLOSED || order.getStatus() == OrderStatus.CANCELED) {
			throw new DataIntegrityViolationException("Pedidos fechados ou cancelados nao podem ser atualizados.");
		}

		DiningTable table = request.getChannel() == OrderChannel.DINE_IN
				? diningTableService.findByNumber(request.getTableNumber())
				: null;
		if (order.getDiningTable() != null
				&& order.getChannel() == OrderChannel.DINE_IN
				&& request.getChannel() == OrderChannel.DINE_IN
				&& !order.getDiningTable().getId().equals(table.getId())) {
			diningTableService.releaseTable(order.getDiningTable().getId());
			diningTableService.reserveTable(table.getId());
		} else if (order.getDiningTable() != null
				&& order.getChannel() == OrderChannel.DINE_IN
				&& request.getChannel() != OrderChannel.DINE_IN) {
			diningTableService.releaseTable(order.getDiningTable().getId());
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
				.orElseThrow(() -> new EntityNotFoundException("Pedido nao encontrado para o id " + id + "."));
	}

	@Transactional(readOnly = true)
	public Page<Order> search(OrderStatus status, List<OrderStatus> statuses, String tableNumber, String code, Long customerId, OrderChannel channel, List<OrderChannel> channels, Pageable pageable) {
		Specification<Order> specification = Specification.where(OrderSpecification.hasStatus(status))
				.and(OrderSpecification.hasAnyStatus(statuses))
				.and(OrderSpecification.hasTableNumber(tableNumber))
				.and(OrderSpecification.hasCode(code))
				.and(OrderSpecification.hasCustomerId(customerId))
				.and(OrderSpecification.hasChannel(channel))
				.and(OrderSpecification.hasAnyChannel(channels));
		List<Order> orders = orderRepository.findAll(specification).stream()
				.sorted(buildOrderComparator())
				.toList();
		int start = (int) pageable.getOffset();
		if (start >= orders.size()) {
			return new PageImpl<>(List.of(), pageable, orders.size());
		}
		int end = Math.min(start + pageable.getPageSize(), orders.size());
		return new PageImpl<>(orders.subList(start, end), pageable, orders.size());
	}

	@Transactional(readOnly = true)
	public Order findActiveOrderByCustomerDocumentNumber(String documentNumber) {
		if (documentNumber == null || documentNumber.isBlank()) {
			return null;
		}
		return customerService.findByDocumentNumber(documentNumber)
				.map(customer -> orderRepository.findFirstByCustomerIdAndStatusInOrderByOpenedAtDesc(
						customer.getId(),
						List.of(OrderStatus.OPEN, OrderStatus.READY_TO_CLOSE)))
				.orElse(null);
	}

	@Transactional
	public Order addItemsFromDigitalMenu(Long id, List<OrderItemRequest> items) {
		validateDigitalOrderingEnabled();
		return addItems(id, items, null);
	}

	@Transactional
	public Order addItems(Long id, List<OrderItemRequest> items, String actorName) {
		if (items == null || items.isEmpty()) {
			throw new DataIntegrityViolationException("Informe ao menos um item para o pedido.");
		}
		Order order = findById(id);
		if (order.getStatus() == OrderStatus.CLOSED || order.getStatus() == OrderStatus.CANCELED) {
			throw new DataIntegrityViolationException("Nao e possivel adicionar itens a pedidos fechados ou cancelados.");
		}
		addItems(order, items);
		recalculateTotals(order);
		auditLogService.register("Order", order.getId(), "ORDER_ITEMS_ADDED", actorName, "Added " + items.size() + " items.");
		Order saved = orderRepository.save(order);
		notifyKitchenOfNewItems(saved);
		return saved;
	}

	private void validateDigitalOrderingEnabled() {
		CompanyProfile companyProfile = companyProfileService.findCurrent();
		if (companyProfile != null && Boolean.FALSE.equals(companyProfile.getDigitalOrderingEnabled())) {
			throw new DataIntegrityViolationException("Pedidos pelo cardapio digital estao desativados.");
		}
	}

	@Transactional
	public OrderCheckoutResponse checkout(Long id, CloseOrderRequest request, String actorName) {
		Order order = findById(id);
		validateOrderReadyForCheckout(order);
		applyCheckoutAdjustments(order, request);
		recalculateTotals(order);

		BigDecimal newPaidAmount = registerPayments(order, request.getPayments(), actorName);
		order.setPaidAmount(order.getPaidAmount().add(newPaidAmount));

		BigDecimal remainingAmount = order.getTotalAmount().subtract(order.getPaidAmount()).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
		BigDecimal changeAmount = calculateChange(request.getPayments(), order.getTotalAmount(), order.getPaidAmount());

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
		return new OrderCheckoutResponse(order, remainingAmount, changeAmount, fullyPaid);
	}

	@Transactional
	public Order updateServiceFee(Long id, boolean applyServiceFee, String actorName) {
		Order order = findById(id);
		if (order.getStatus() == OrderStatus.CLOSED || order.getStatus() == OrderStatus.CANCELED) {
			throw new DataIntegrityViolationException("Pedidos fechados ou cancelados nao permitem alterar a taxa de servico.");
		}
		order.setApplyServiceFee(applyServiceFee);
		recalculateTotals(order);
		auditLogService.register(
				"Order",
				order.getId(),
				"ORDER_SERVICE_FEE_UPDATED",
				actorName,
				"applyServiceFee=" + applyServiceFee);
		return orderRepository.save(order);
	}

	@Transactional
	public Order requestCheckout(Long id, RequestOrderCheckoutRequest request, String actorName) {
		Order order = findById(id);
		if (order.getStatus() == OrderStatus.CLOSED || order.getStatus() == OrderStatus.CANCELED) {
			throw new DataIntegrityViolationException("Pedidos fechados ou cancelados nao podem solicitar a conta.");
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
		Order saved = orderRepository.save(order);
		notifyTablesOfCheckoutRequest(saved);
		return saved;
	}

	private void notifyTablesOfCheckoutRequest(Order order) {
		String code = order.getCode() != null ? order.getCode() : ("#" + order.getId());
		String origin = describeOrderOrigin(order);
		String customerName = order.getCustomer() != null && order.getCustomer().getName() != null
				? order.getCustomer().getName()
				: "Cliente";
		pushNotificationService.notifyTopic(
				com.br.food.models.PushSubscription.TOPIC_TABLES,
				"Conta solicitada — " + origin,
				customerName + " pediu a conta no pedido " + code + ".",
				"/admin/mesas");
	}

	@Transactional
	public void cancelItem(Long orderId, Long itemId, String reason, String actorName) {
		Order order = findById(orderId);
		OrderItem item = order.getItems().stream()
				.filter(orderItem -> orderItem.getId().equals(itemId))
				.findFirst()
				.orElseThrow(() -> new EntityNotFoundException("Item do pedido nao encontrado para o id " + itemId + "."));
		if (item.getStatus() == OrderItemStatus.SERVED) {
			throw new DataIntegrityViolationException("Itens ja entregues nao podem ser cancelados.");
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
		handleOrderWithoutActiveItems(order);
		auditLogService.register("OrderItem", item.getId(), "ORDER_ITEM_CANCELED", actorName, reason);
	}

	@Transactional
	public void cancelOrder(Long orderId, String reason, String actorName) {
		Order order = findById(orderId);
		if (order.getStatus() == OrderStatus.CLOSED) {
			throw new DataIntegrityViolationException("Pedidos fechados nao podem ser cancelados.");
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
			throw new DataIntegrityViolationException("Apenas pedidos fechados podem ser reabertos.");
		}
		if (order.getDiningTable() != null && order.getChannel() == OrderChannel.DINE_IN) {
			if (!Boolean.TRUE.equals(order.getDiningTable().getActive())) {
				throw new DataIntegrityViolationException("Nao e possivel reabrir o pedido porque a mesa original esta inativa.");
			}
			if (Boolean.TRUE.equals(order.getDiningTable().getOccupied())) {
				throw new DataIntegrityViolationException(
						"Nao e possivel reabrir o pedido porque a mesa " + order.getDiningTable().getNumber() + " esta ocupada no momento.");
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
			throw new DataIntegrityViolationException("Pedidos fechados ou cancelados nao podem ser transferidos.");
		}
		if (order.getChannel() != OrderChannel.DINE_IN) {
			throw new DataIntegrityViolationException("Apenas pedidos no salao podem ser transferidos entre mesas.");
		}
		DiningTable targetTable = diningTableService.findByNumber(targetTableNumber);
		if (order.getDiningTable() != null && order.getDiningTable().getId().equals(targetTable.getId())) {
			throw new DataIntegrityViolationException("O pedido ja esta vinculado a mesa selecionada.");
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
			throw new DataIntegrityViolationException("O pedido de origem e o de destino devem ser diferentes.");
		}
		if (targetOrder.getStatus() == OrderStatus.CLOSED || targetOrder.getStatus() == OrderStatus.CANCELED
				|| sourceOrder.getStatus() == OrderStatus.CLOSED || sourceOrder.getStatus() == OrderStatus.CANCELED) {
			throw new DataIntegrityViolationException("Apenas pedidos ativos podem ser mesclados.");
		}
		if (targetOrder.getChannel() != sourceOrder.getChannel()) {
			throw new DataIntegrityViolationException("Pedidos de canais diferentes nao podem ser mesclados.");
		}
		if (targetOrder.getChannel() == OrderChannel.DINE_IN) {
			String targetTableNumber = targetOrder.getDiningTable() != null ? targetOrder.getDiningTable().getNumber() : null;
			String sourceTableNumber = sourceOrder.getDiningTable() != null ? sourceOrder.getDiningTable().getNumber() : null;
			if (targetTableNumber == null || sourceTableNumber == null || !targetTableNumber.equals(sourceTableNumber)) {
				throw new DataIntegrityViolationException("Pedidos no salao so podem ser mesclados quando pertencem a mesma mesa.");
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
			throw new DataIntegrityViolationException("Pedidos fechados ou cancelados nao podem ser divididos.");
		}
		if (sourceOrder.getChannel() != OrderChannel.DINE_IN) {
			throw new DataIntegrityViolationException("Apenas pedidos no salao podem ser divididos entre mesas.");
		}
		DiningTable destinationTable = diningTableService.findById(destinationTableId);
		if (sourceOrder.getDiningTable() != null && sourceOrder.getDiningTable().getId().equals(destinationTableId)) {
			throw new DataIntegrityViolationException("A mesa de destino da divisao deve ser diferente da atual.");
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
			throw new DataIntegrityViolationException("Selecione ao menos um item para dividir.");
		}
		long movableItemsCount = sourceOrder.getItems().stream()
				.filter(item -> item.getStatus() != OrderItemStatus.CANCELED && item.getStatus() != OrderItemStatus.DECLINED)
				.count();
		if (selectedItems.size() >= movableItemsCount) {
			throw new DataIntegrityViolationException("A divisao precisa manter ao menos um item no pedido original. Use a transferencia para mover o pedido inteiro.");
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
				.orElseThrow(() -> new EntityNotFoundException("Item do pedido nao encontrado para o id " + itemId + "."));
		OrderItemStatus.validateTransition(item.getStatus(), OrderItemStatus.SERVED);
		item.setStatus(OrderItemStatus.SERVED);
		refreshOrderStatus(order);
		auditLogService.register("OrderItem", itemId, "ORDER_ITEM_SERVED", actorName, "Item served.");
	}

	@Transactional
	public void restoreConsumedStock(OrderItem orderItem) {
		if (orderItem == null || orderItem.getStockConsumptions().isEmpty()) {
			return;
		}
		stockEntryService.restoreConsumptions(orderItem);
	}

	@Transactional
	public void recalculateOrderAfterKitchenRejection(Long orderId) {
		Order order = findById(orderId);
		recalculateTotals(order);
		refundRegisteredPaymentsIfNeeded(order);
		handleOrderWithoutActiveItems(order);
	}

	@Transactional
	public void refreshOrderStatus(Long orderId) {
		Order order = findById(orderId);
		refreshOrderStatus(order);
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
		if (request.getApplyServiceFee() != null) {
			order.setApplyServiceFee(request.getApplyServiceFee());
		}
		if (request.getDiscountPercentage() != null) {
			order.setDiscountPercentage(request.getDiscountPercentage().setScale(2, RoundingMode.HALF_UP));
		}
		if (request.getDiscountAmount() != null) {
			order.setDiscountAmount(request.getDiscountAmount().setScale(2, RoundingMode.HALF_UP));
		}
	}

	private void validateOrderRequest(OrderRequest request) {
		if (request.getItems() == null || request.getItems().isEmpty()) {
			throw new DataIntegrityViolationException("Pedidos precisam conter ao menos um item.");
		}
		if (request.getChannel() == OrderChannel.DINE_IN
				&& (request.getTableNumber() == null || request.getTableNumber().isBlank())) {
			throw new DataIntegrityViolationException("Informe o numero da mesa para pedidos no salao.");
		}
		if (request.getChannel() != OrderChannel.DINE_IN) {
			validateRemotePayment(request);
		}
	}

	private void validateRemotePayment(OrderRequest request) {
		PaymentMethod method = request.getPaymentMethod();

		if (method == null) {
			throw new DataIntegrityViolationException(
					"Informe a forma de pagamento para pedidos de entrega ou retirada.");
		}
		if (request.getChangeForAmount() != null && method != PaymentMethod.CASH) {
			throw new DataIntegrityViolationException(
					"Troco so se aplica a pagamentos em dinheiro.");
		}
	}

	private void validateChannelEnabled(OrderChannel channel) {
		CompanyProfile companyProfile = companyProfileService.findCurrent();
		if (companyProfile == null) {
			return;
		}
		if (channel == OrderChannel.DINE_IN && Boolean.FALSE.equals(companyProfile.getDineInEnabled())) {
			throw new DataIntegrityViolationException("Pedidos no salao estao desativados.");
		}
		if (channel == OrderChannel.DELIVERY && Boolean.FALSE.equals(companyProfile.getDeliveryEnabled())) {
			throw new DataIntegrityViolationException("Pedidos de entrega estao desativados.");
		}
		if (channel == OrderChannel.TAKEAWAY && Boolean.FALSE.equals(companyProfile.getTakeawayEnabled())) {
			throw new DataIntegrityViolationException("Pedidos para retirada estao desativados.");
		}
	}

	private void addItems(Order order, List<OrderItemRequest> items) {
		Map<Long, List<OrderItemRequest>> promotionItems = new LinkedHashMap<>();

		for (OrderItemRequest itemRequest : items) {
			if (itemRequest.getPromotionId() != null) {
				promotionItems.computeIfAbsent(itemRequest.getPromotionId(), key -> new ArrayList<>()).add(itemRequest);
				continue;
			}

			order.getItems().add(createOrderItem(order, itemRequest, null));
		}

		for (Map.Entry<Long, List<OrderItemRequest>> promotionEntry : promotionItems.entrySet()) {
			addPromotionItems(order, promotionEntry.getKey(), promotionEntry.getValue());
		}

		refreshOrderStatus(order);
	}

	private OrderItem createOrderItem(Order order, OrderItemRequest itemRequest, BigDecimal unitPriceOverride) {
		Product product = productService.findById(itemRequest.getProductId());
		validateProductForOrder(product);
		List<ProductVariation> selectedVariations = resolveSelectedVariations(product, itemRequest.getProductVariationIds());
		BigDecimal unitPrice = unitPriceOverride != null
				? unitPriceOverride.setScale(2, RoundingMode.HALF_UP)
				: calculateCustomizedUnitPrice(product, itemRequest, selectedVariations);
		OrderItem orderItem = new OrderItem(order, product, itemRequest, unitPrice);
		for (int index = 0; index < selectedVariations.size(); index++) {
			orderItem.getVariations().add(new OrderItemVariation(orderItem, selectedVariations.get(index), index));
		}
		applyCustomizedIngredients(orderItem, product, itemRequest);
		return orderItem;
	}

	private void addPromotionItems(Order order, Long promotionId, List<OrderItemRequest> items) {
		Promotion promotion = promotionService.findById(promotionId);
		validatePromotionForOrder(promotion, items);

		List<OrderItem> promotionOrderItems = items.stream()
				.map(itemRequest -> createOrderItem(order, itemRequest, null))
				.toList();

		BigDecimal rawPromotionTotal = promotionOrderItems.stream()
				.map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		BigDecimal promotionTotal = promotion.getPromotionPrice().setScale(2, RoundingMode.HALF_UP);
		if (rawPromotionTotal.compareTo(BigDecimal.ZERO) <= 0) {
			throw new DataIntegrityViolationException("Os itens da promocao precisam gerar um total valido.");
		}

		BigDecimal allocatedTotal = BigDecimal.ZERO;
		for (int index = 0; index < promotionOrderItems.size(); index++) {
			OrderItem orderItem = promotionOrderItems.get(index);
			BigDecimal lineRawTotal = orderItem.getUnitPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity()));
			BigDecimal adjustedLineTotal;

			if (index == promotionOrderItems.size() - 1) {
				adjustedLineTotal = promotionTotal.subtract(allocatedTotal);
			} else {
				adjustedLineTotal = promotionTotal
						.multiply(lineRawTotal)
						.divide(rawPromotionTotal, 2, RoundingMode.HALF_UP);
				allocatedTotal = allocatedTotal.add(adjustedLineTotal);
			}

			BigDecimal adjustedUnitPrice = adjustedLineTotal
					.divide(BigDecimal.valueOf(orderItem.getQuantity()), 2, RoundingMode.HALF_UP);
			orderItem.setUnitPrice(adjustedUnitPrice);
			order.getItems().add(orderItem);
		}
	}

	private void validatePromotionForOrder(Promotion promotion, List<OrderItemRequest> items) {
		if (!Boolean.TRUE.equals(promotion.getActive())
				|| (promotion.getExpiresAt() != null && promotion.getExpiresAt().isBefore(java.time.LocalDate.now()))) {
			throw new DataIntegrityViolationException("Esta promocao nao esta mais disponivel.");
		}

		Set<Long> requestedProductIds = items.stream()
				.map(OrderItemRequest::getProductId)
				.collect(Collectors.toSet());
		Set<Long> promotionProductIds = promotion.getProducts().stream()
				.map(Product::getId)
				.collect(Collectors.toSet());

		if (!requestedProductIds.equals(promotionProductIds)) {
			throw new DataIntegrityViolationException("Os itens da promocao precisam corresponder aos produtos configurados nela.");
		}

		for (OrderItemRequest itemRequest : items) {
			Product product = productService.findById(itemRequest.getProductId());
			validateProductForOrder(product);
		}
	}

	private void validateProductForOrder(Product product) {
		if (!Boolean.TRUE.equals(product.getActive())) {
			throw new DataIntegrityViolationException("Produtos inativos nao podem ser pedidos.");
		}
		if (Boolean.TRUE.equals(product.getComplement())) {
			throw new DataIntegrityViolationException("Produtos complementares nao podem ser pedidos isoladamente.");
		}
	}

	private BigDecimal calculateCustomizedUnitPrice(Product product, OrderItemRequest itemRequest,
			List<ProductVariation> selectedVariations) {
		BigDecimal unitPrice = product.getPrice().setScale(2, RoundingMode.HALF_UP);
		for (ProductVariation variation : selectedVariations) {
			unitPrice = unitPrice.add(variation.getPriceDelta().setScale(2, RoundingMode.HALF_UP));
		}
		Map<Long, RecipeItem> recipeItemsByIngredientId = buildRecipeItemsMap(product);

		for (OrderItemIngredientRequest ingredientRequest : itemRequest.getIngredients()) {
			Product ingredientProduct = productService.findById(ingredientRequest.getIngredientProductId());
			if (ingredientProduct.getType() != ProductType.INGREDIENT) {
				throw new DataIntegrityViolationException("Ingredientes personalizados precisam usar produtos do tipo INGREDIENT.");
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
			throw new DataIntegrityViolationException("Nao e possivel fechar pedidos sem itens.");
		}
		boolean hasOpenKitchenItem = order.getItems().stream()
				.filter(item -> item.getStatus() != OrderItemStatus.CANCELED && item.getStatus() != OrderItemStatus.DECLINED)
				.filter(item -> item.getProduct() != null && Boolean.TRUE.equals(item.getProduct().getSendToKitchen()))
				.anyMatch(item -> item.getStatus() != OrderItemStatus.READY && item.getStatus() != OrderItemStatus.SERVED);
		if (hasOpenKitchenItem) {
			throw new DataIntegrityViolationException("Ainda existem itens pendentes no fluxo da cozinha.");
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
			throw new DataIntegrityViolationException("O valor recebido em dinheiro precisa ser maior ou igual ao valor pago em dinheiro.");
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
		if (order.getChannel() != OrderChannel.DINE_IN || !Boolean.TRUE.equals(order.getApplyServiceFee())) {
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
		List<OrderItem> activeItems = getActiveOrderItems(order);
		if (activeItems.isEmpty()) {
			order.setStatus(OrderStatus.CANCELED);
			order.setCheckoutRequestedAt(null);
			order.setRequestedPaymentMethod(null);
			order.setCheckoutRequestNotes(null);
			if (order.getDiningTable() != null && order.getChannel() == OrderChannel.DINE_IN) {
				diningTableService.releaseTable(order.getDiningTable().getId());
			}
			return;
		}
		boolean allCompleted = activeItems.stream()
				.allMatch(item -> item.getStatus() == OrderItemStatus.READY || item.getStatus() == OrderItemStatus.SERVED);
		order.setStatus(allCompleted ? OrderStatus.READY_TO_CLOSE : OrderStatus.OPEN);
	}

	private void handleOrderWithoutActiveItems(Order order) {
		if (!getActiveOrderItems(order).isEmpty()) {
			return;
		}
		order.setPaidAmount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
	}

	private List<OrderItem> getActiveOrderItems(Order order) {
		return order.getItems().stream()
				.filter(item -> item.getStatus() != OrderItemStatus.CANCELED && item.getStatus() != OrderItemStatus.DECLINED)
				.toList();
	}

	private List<ProductVariation> resolveSelectedVariations(Product product, List<Long> selectedIds) {
		List<ProductVariationGroup> activeGroups = product.getVariationGroups().stream()
				.filter(group -> Boolean.TRUE.equals(group.getActive()))
				.filter(group -> group.getVariations().stream().anyMatch(variation -> Boolean.TRUE.equals(variation.getActive())))
				.toList();

		List<Long> requestedIds = selectedIds != null ? selectedIds : List.of();

		if (activeGroups.isEmpty()) {
			if (!requestedIds.isEmpty()) {
				throw new DataIntegrityViolationException("Este produto nao aceita selecao de variacoes.");
			}
			return List.of();
		}

		if (requestedIds.size() != activeGroups.size()) {
			throw new DataIntegrityViolationException("Selecione uma opcao para cada grupo de variacao deste produto.");
		}

		List<ProductVariation> resolved = new ArrayList<>();
		Set<Long> coveredGroupIds = new java.util.HashSet<>();

		for (Long variationId : requestedIds) {
			if (variationId == null) {
				throw new DataIntegrityViolationException("O id da variacao selecionada nao pode ser nulo.");
			}

			ProductVariation match = activeGroups.stream()
					.flatMap(group -> group.getVariations().stream())
					.filter(variation -> variation.getId().equals(variationId))
					.filter(variation -> Boolean.TRUE.equals(variation.getActive()))
					.findFirst()
					.orElseThrow(() -> new DataIntegrityViolationException("A variacao selecionada do produto e invalida."));

			Long groupId = match.getGroup() != null ? match.getGroup().getId() : null;
			if (groupId == null || !coveredGroupIds.add(groupId)) {
				throw new DataIntegrityViolationException("Selecione apenas uma opcao por grupo de variacao.");
			}

			resolved.add(match);
		}

		return resolved;
	}

	private Comparator<Order> buildOrderComparator() {
		return Comparator
				.comparingInt(this::getOrderSortingPriority)
				.thenComparing(Order::getOpenedAt, Comparator.nullsLast(Comparator.reverseOrder()))
				.thenComparing(Order::getCode, Comparator.nullsLast(Comparator.reverseOrder()));
	}

	private int getOrderSortingPriority(Order order) {
		if (order.getStatus() == OrderStatus.READY_TO_CLOSE) {
			return 0;
		}
		if (order.getStatus() == OrderStatus.OPEN) {
			boolean hasReadyItems = getActiveOrderItems(order).stream().anyMatch(item -> item.getStatus() == OrderItemStatus.READY);
			if (hasReadyItems) {
				return 1;
			}
			boolean hasKitchenPendingItems = getActiveOrderItems(order).stream()
					.anyMatch(item -> item.getStatus() == OrderItemStatus.RECEIVED
							|| item.getStatus() == OrderItemStatus.QUEUED
							|| item.getStatus() == OrderItemStatus.IN_PREPARATION);
			if (hasKitchenPendingItems) {
				return 2;
			}
			return 3;
		}
		if (order.getStatus() == OrderStatus.CLOSED) {
			return 4;
		}
		return 5;
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
