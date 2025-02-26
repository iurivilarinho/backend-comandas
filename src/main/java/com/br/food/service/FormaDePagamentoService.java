package com.br.food.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.br.food.enums.Tipos.TipoPagamento;
import com.br.food.models.FormaDePagamento;
import com.br.food.repository.FormaDePagamentoRepository;

@Service
public class FormaDePagamentoService {

	@Autowired
	private FormaDePagamentoRepository formaDePagamentoRepository;

	@Transactional(readOnly = true)
	public FormaDePagamento buscarPorTipoPagamento(TipoPagamento tipo) {
		return formaDePagamentoRepository.findByTipoPagamento(tipo)
				.orElseThrow(() -> new RuntimeException("Forma de pagamento não encontrada para o tipo " + tipo));
	}
}
