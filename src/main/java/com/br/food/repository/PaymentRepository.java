package com.br.food.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.br.food.enums.Types.PaymentMethod;
import com.br.food.models.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

	Optional<Payment> findByPaymentMethod(PaymentMethod paymentMethod);

}
