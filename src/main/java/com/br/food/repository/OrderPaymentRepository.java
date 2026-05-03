package com.br.food.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.br.food.models.OrderPayment;

@Repository
public interface OrderPaymentRepository extends JpaRepository<OrderPayment, Long> {
}
