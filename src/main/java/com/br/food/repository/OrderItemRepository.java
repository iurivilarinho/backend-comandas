package com.br.food.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.br.food.enums.Types.OrderItemStatus;
import com.br.food.models.OrderItem;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

	List<OrderItem> findByStatusInOrderByRequestedAtAsc(List<OrderItemStatus> statuses);
}
