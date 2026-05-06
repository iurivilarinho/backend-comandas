package com.br.food.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.br.food.enums.Types.OrderItemStatus;
import com.br.food.models.OrderItem;
import com.br.food.repository.OrderItemRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class OrderItemService {

	private final OrderItemRepository orderItemRepository;

	public OrderItemService(OrderItemRepository orderItemRepository) {
		this.orderItemRepository = orderItemRepository;
	}

	@Transactional(readOnly = true)
	public OrderItem findById(Long id) {
		return orderItemRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Item do pedido nao encontrado para o id " + id + "."));
	}

	@Transactional
	public void addDeclineReason(Long id, String declineReason) {
		OrderItem item = findById(id);
		item.setDeclineReason(declineReason);
	}

	@Transactional
	public void updateStatus(Long id, OrderItemStatus targetStatus) {
		OrderItem item = findById(id);
		OrderItemStatus.validateTransition(item.getStatus(), targetStatus);
		item.setStatus(targetStatus);
	}
}
