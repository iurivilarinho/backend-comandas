package com.br.food.repository;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.br.food.enums.Types.OrderItemStatus;
import com.br.food.enums.Types.OrderStatus;
import com.br.food.models.Order;
import com.br.food.models.OrderItem;

import jakarta.persistence.criteria.Join;

public final class OrderSpecification {

	private OrderSpecification() {
	}

	public static Specification<Order> hasKitchenPendingItems() {
		return (root, query, builder) -> {
			Join<Order, OrderItem> itemsJoin = root.join("items");
			query.distinct(true);
			return itemsJoin.get("status").in(OrderItemStatus.RECEIVED, OrderItemStatus.QUEUED, OrderItemStatus.IN_PREPARATION);
		};
	}

	public static Specification<Order> hasStatus(OrderStatus status) {
		return (root, query, builder) -> status == null ? builder.conjunction() : builder.equal(root.get("status"), status);
	}

	public static Specification<Order> hasAnyStatus(List<OrderStatus> statuses) {
		return (root, query, builder) -> statuses == null || statuses.isEmpty()
				? builder.conjunction()
				: root.get("status").in(statuses);
	}

	public static Specification<Order> hasTableNumber(String tableNumber) {
		return (root, query, builder) -> tableNumber == null || tableNumber.isBlank()
				? builder.conjunction()
				: builder.equal(root.join("diningTable").get("number"), tableNumber);
	}

	public static Specification<Order> hasCode(String code) {
		return (root, query, builder) -> code == null || code.isBlank()
				? builder.conjunction()
				: builder.like(builder.upper(root.get("code")), "%" + code.trim().toUpperCase() + "%");
	}
}
