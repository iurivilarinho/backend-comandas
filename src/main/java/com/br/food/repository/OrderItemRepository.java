package com.br.food.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.br.food.models.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

}
