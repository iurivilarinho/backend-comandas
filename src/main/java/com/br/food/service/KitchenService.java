package com.br.food.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.br.food.enums.Types.OrderItemStatus;
import com.br.food.enums.Types.ProductType;
import com.br.food.models.OrderItem;
import com.br.food.repository.OrderRepository;
import com.br.food.repository.OrderSpecification;
import com.br.food.response.PendingOrderResponse;

@Service
public class KitchenService {

	private final OrderRepository orderRepository;
	private final OrderItemService orderItemService;

	public KitchenService(OrderRepository orderRepository, OrderItemService orderItemService) {
		this.orderRepository = orderRepository;
		this.orderItemService = orderItemService;
	}

	@Transactional
	public void acceptOrderItem(Long orderItemId) {
		OrderItem item = orderItemService.findById(orderItemId);
		validateCurrentStatus(item);

		OrderItemStatus nextStatus = item.getProduct().getType() == ProductType.FINISHED
				? OrderItemStatus.SERVED
				: OrderItemStatus.IN_PREPARATION;
		orderItemService.updateStatus(orderItemId, nextStatus);
	}

	@Transactional
	public void rejectOrderItem(Long orderItemId, String declineReason) {
		OrderItem item = orderItemService.findById(orderItemId);
		validateCurrentStatus(item);
		orderItemService.addDeclineReason(orderItemId, declineReason);
		orderItemService.updateStatus(orderItemId, OrderItemStatus.DECLINED);
	}

	@Transactional(readOnly = true)
	public Page<PendingOrderResponse> listPendingItems(Pageable pageable) {
		return orderRepository.findAll(OrderSpecification.hasPendingItems(), pageable).map(PendingOrderResponse::new);
	}

	private void validateCurrentStatus(OrderItem item) {
		if (item.getStatus() == OrderItemStatus.SERVED) {
			throw new DataIntegrityViolationException("This item has already been served.");
		}
		if (item.getStatus() == OrderItemStatus.DECLINED) {
			throw new DataIntegrityViolationException("This item has already been declined.");
		}
	}
}
