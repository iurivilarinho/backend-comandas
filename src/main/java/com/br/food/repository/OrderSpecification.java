package com.br.food.repository;

import org.springframework.data.jpa.domain.Specification;

import com.br.food.enums.Types.OrderItemStatus;
import com.br.food.models.Order;
import com.br.food.models.OrderItem;

import jakarta.persistence.criteria.Join;

public final class OrderSpecification {

	private OrderSpecification() {
	}

	public static Specification<Order> hasPendingItems() {
		return (root, query, builder) -> {
			Join<Order, OrderItem> itemsJoin = root.join("items");
			return builder.equal(itemsJoin.get("status"), OrderItemStatus.PENDING);
		};
	}
}
