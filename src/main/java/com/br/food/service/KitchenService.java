package com.br.food.service;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.br.food.enums.Types.OrderItemStatus;
import com.br.food.models.Order;
import com.br.food.models.OrderItem;
import com.br.food.repository.OrderItemRepository;
import com.br.food.response.PendingOrderResponse;

@Service
public class KitchenService {

	private final OrderItemRepository orderItemRepository;
	private final OrderItemService orderItemService;
	private final OrderService orderService;
	private final AuditLogService auditLogService;

	public KitchenService(
			OrderItemRepository orderItemRepository,
			OrderItemService orderItemService,
			OrderService orderService,
			AuditLogService auditLogService) {
		this.orderItemRepository = orderItemRepository;
		this.orderItemService = orderItemService;
		this.orderService = orderService;
		this.auditLogService = auditLogService;
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
			throw new DataIntegrityViolationException("Only items in preparation can be marked as ready.");
		}
		orderItemService.updateStatus(orderItemId, OrderItemStatus.READY);
		auditLogService.register("OrderItem", orderItemId, "KITCHEN_MARKED_READY", actorName, "Item ready for service.");
	}

	@Transactional
	public void rejectOrderItem(Long orderItemId, String declineReason, String actorName) {
		OrderItem item = orderItemService.findById(orderItemId);
		validateCurrentStatus(item);
		orderItemService.addDeclineReason(orderItemId, declineReason);
		orderItemService.updateStatus(orderItemId, OrderItemStatus.DECLINED);
		auditLogService.register("OrderItem", orderItemId, "KITCHEN_DECLINED_ITEM", actorName, declineReason);
	}

	private void validateCurrentStatus(OrderItem item) {
		if (item.getStatus() == OrderItemStatus.SERVED) {
			throw new DataIntegrityViolationException("This item has already been served.");
		}
		if (item.getStatus() == OrderItemStatus.DECLINED) {
			throw new DataIntegrityViolationException("This item has already been declined.");
		}
		if (item.getStatus() == OrderItemStatus.CANCELED) {
			throw new DataIntegrityViolationException("This item has already been canceled.");
		}
	}
}
