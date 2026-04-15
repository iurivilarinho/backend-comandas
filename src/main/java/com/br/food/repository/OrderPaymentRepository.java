package com.br.food.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.br.food.models.OrderPayment;

public interface OrderPaymentRepository extends JpaRepository<OrderPayment, Long> {
}
