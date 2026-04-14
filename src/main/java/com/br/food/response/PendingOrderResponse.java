package com.br.food.response;

import java.util.List;

import com.br.food.enums.Types.OrderChannel;
import com.br.food.enums.Types.OrderItemStatus;
import com.br.food.enums.Types.OrderStatus;
import com.br.food.models.Order;

public class PendingOrderResponse {

	private final Long id;
	private final CustomerResponse customer;
	private final DiningTableResponse diningTable;
	private final List<OrderItemResponse> items;
	private final String code;
	private final OrderStatus status;
	private final OrderChannel channel;

	public PendingOrderResponse(Order order) {
		this.id = order.getId();
		this.customer = order.getCustomer() != null ? new CustomerResponse(order.getCustomer()) : null;
		this.diningTable = order.getDiningTable() != null ? new DiningTableResponse(order.getDiningTable()) : null;
		this.items = order.getItems().stream()
				.filter(item -> item.getStatus() == OrderItemStatus.PENDING)
				.map(OrderItemResponse::new)
				.toList();
		this.code = order.getCode();
		this.status = order.getStatus();
		this.channel = order.getChannel();
	}

	public Long getId() {
		return id;
	}

	public CustomerResponse getCustomer() {
		return customer;
	}

	public DiningTableResponse getDiningTable() {
		return diningTable;
	}

	public List<OrderItemResponse> getItems() {
		return items;
	}

	public String getCode() {
		return code;
	}

	public OrderStatus getStatus() {
		return status;
	}

	public OrderChannel getChannel() {
		return channel;
	}
}
