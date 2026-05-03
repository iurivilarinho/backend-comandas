package com.br.food.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.br.food.enums.Types.PaymentMethod;
import com.br.food.models.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

	Optional<Payment> findByPaymentMethod(PaymentMethod paymentMethod);

}
