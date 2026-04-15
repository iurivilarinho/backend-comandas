package com.br.food.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.br.food.enums.Types.OrderItemStatus;
import com.br.food.models.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

	List<OrderItem> findByStatusInOrderByRequestedAtAsc(List<OrderItemStatus> statuses);
}
