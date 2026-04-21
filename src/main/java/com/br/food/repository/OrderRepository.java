package com.br.food.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.br.food.enums.Types.OrderStatus;
import com.br.food.models.Order;

public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

	Order findTopByOrderByCodeDesc();

	Order findFirstByCustomerIdAndStatusInOrderByOpenedAtDesc(Long customerId, List<OrderStatus> statuses);
}
