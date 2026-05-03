package com.br.food.service;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.br.food.enums.Types.OrderItemStatus;
import com.br.food.models.OrderItem;
import com.br.food.repository.OrderItemRepository;
import com.br.food.response.PendingOrderResponse;

@Service
public class KitchenService {

	private final OrderItemRepository orderItemRepository;
	private final OrderItemService orderItemService;
	private final OrderService orderService;
	private final AuditLogService auditLogService;
	private final PushNotificationService pushNotificationService;

	public KitchenService(
			OrderItemRepository orderItemRepository,
			OrderItemService orderItemService,
			OrderService orderService,
			AuditLogService auditLogService,
			PushNotificationService pushNotificationService) {
		this.orderItemRepository = orderItemRepository;
		this.orderItemService = orderItemService;
		this.orderService = orderService;
		this.auditLogService = auditLogService;
		this.pushNotificationService = pushNotificationService;
	}

	@Transactional(readOnly = true)
	public List<PendingOrderResponse> listPendingItems() {
		return orderItemRepository.findByStatusInOrderByRequestedAtAsc(List.of(
				OrderItemStatus.RECEIVED,
				OrderItemStatus.QUEUED,
				OrderItemStatus.IN_PREPARATION))
				.stream()
				.map(OrderItem::getOrder)
				.distinct()
				.map(PendingOrderResponse::new)
				.toList();
	}

	@Transactional
	public void acceptOrderItem(Long orderItemId, String actorName) {
		OrderItem item = orderItemService.findById(orderItemId);
		validateCurrentStatus(item);
		orderItemService.updateStatus(orderItemId, OrderItemStatus.QUEUED);
		auditLogService.register("OrderItem", orderItemId, "KITCHEN_ACCEPTED_ITEM", actorName, "Moved to queued.");
	}

	@Transactional
	public void startPreparation(Long orderItemId, String actorName) {
		OrderItem item = orderItemService.findById(orderItemId);
		validateCurrentStatus(item);
		orderItemService.updateStatus(orderItemId, OrderItemStatus.IN_PREPARATION);
		orderService.consumeRecipeForItem(item);
		auditLogService.register("OrderItem", orderItemId, "KITCHEN_STARTED_PREPARATION", actorName, "Preparation started.");
	}

	@Transactional
	public void markReady(Long orderItemId, String actorName) {
		OrderItem item = orderItemService.findById(orderItemId);
		if (item.getStatus() != OrderItemStatus.IN_PREPARATION) {
			throw new DataIntegrityViolationException("Somente itens em preparo podem ser marcados como prontos.");
		}
		orderItemService.updateStatus(orderItemId, OrderItemStatus.READY);
		auditLogService.register("OrderItem", orderItemId, "KITCHEN_MARKED_READY", actorName, "Item ready for service.");

		Long customerId = item.getOrder() != null && item.getOrder().getCustomer() != null
				? item.getOrder().getCustomer().getId()
				: null;
		String productLabel = item.getProduct() != null && item.getProduct().getDescription() != null
				? item.getProduct().getDescription()
				: "Seu pedido";
		pushNotificationService.notifyCustomer(
				customerId,
				"Seu pedido está pronto",
				productLabel + " já saiu da cozinha.",
				"/conta");
	}

	@Transactional
	public void rejectOrderItem(Long orderItemId, String declineReason, String actorName) {
		OrderItem item = orderItemService.findById(orderItemId);
		validateCurrentStatus(item);
		if (!item.getStockConsumptions().isEmpty()) {
			orderService.restoreConsumedStock(item);
		}
		orderItemService.addDeclineReason(orderItemId, declineReason);
		orderItemService.updateStatus(orderItemId, OrderItemStatus.DECLINED);
		orderService.recalculateOrderAfterKitchenRejection(item.getOrder().getId());
		auditLogService.register("OrderItem", orderItemId, "KITCHEN_DECLINED_ITEM", actorName, declineReason);
	}

	private void validateCurrentStatus(OrderItem item) {
		if (item.getStatus() == OrderItemStatus.SERVED) {
			throw new DataIntegrityViolationException("Este item ja foi entregue.");
		}
		if (item.getStatus() == OrderItemStatus.DECLINED) {
			throw new DataIntegrityViolationException("Este item ja foi recusado.");
		}
		if (item.getStatus() == OrderItemStatus.CANCELED) {
			throw new DataIntegrityViolationException("Este item ja foi cancelado.");
		}
	}
}
