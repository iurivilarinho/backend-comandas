package com.br.food.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.br.food.enums.Tipos.TipoPagamento;
import com.br.food.models.FormaDePagamento;

@Repository
public interface FormaDePagamentoRepository extends JpaRepository<FormaDePagamento, Long> {

	Optional<FormaDePagamento> findByTipoPagamento(TipoPagamento tipo);

}
