package com.br.food.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.br.food.enums.Types.PaymentMethod;
import com.br.food.models.Payment;
import com.br.food.repository.PaymentRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class PaymentService {

	private final PaymentRepository paymentRepository;

	public PaymentService(PaymentRepository paymentRepository) {
		this.paymentRepository = paymentRepository;
	}

	@Transactional(readOnly = true)
	public Payment findByPaymentMethod(PaymentMethod paymentMethod) {
		return paymentRepository.findByPaymentMethod(paymentMethod)
				.orElseThrow(() -> new EntityNotFoundException("Configuracao da forma de pagamento nao encontrada para " + paymentMethod + "."));
	}

	@Transactional(readOnly = true)
	public List<Payment> findAll() {
		return paymentRepository.findAll();
	}

	@Transactional
	public void ensureDefaultMethods() {
		Arrays.stream(PaymentMethod.values()).forEach(paymentMethod -> paymentRepository.findByPaymentMethod(paymentMethod)
				.orElseGet(() -> paymentRepository.save(new Payment(paymentMethod))));
	}
}
