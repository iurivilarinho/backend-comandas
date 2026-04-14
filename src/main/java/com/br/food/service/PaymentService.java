package com.br.food.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.br.food.enums.Types.PaymentMethod;
import com.br.food.models.Payment;
import com.br.food.repository.PaymentRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class PaymentService {

	private final PaymentRepository formaDePagamentoRepository;

	public PaymentService(PaymentRepository formaDePagamentoRepository) {
		this.formaDePagamentoRepository = formaDePagamentoRepository;
	}

	@Transactional(readOnly = true)
	public Payment findByPaymentMethod(PaymentMethod paymentMethod) {
		return formaDePagamentoRepository.findByTipoPagamento(paymentMethod)
				.orElseThrow(() -> new EntityNotFoundException(
						"Payment method configuration not found for " + paymentMethod + "."));
	}

	@Transactional
	public Payment createOrReuse(PaymentMethod paymentMethod, BigDecimal paidAmount) {
		return formaDePagamentoRepository.findByTipoPagamento(paymentMethod)
				.map(existingMethod -> {
					existingMethod.setValorPago(paidAmount);
					return existingMethod;
				})
				.orElseGet(() -> formaDePagamentoRepository.save(new Payment(paymentMethod, paidAmount)));
	}
}
