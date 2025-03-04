package com.br.food.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.br.food.enums.Status.StatusNotaFiscal;
import com.br.food.models.NotaFiscal;

@Repository
public interface NotaFiscalRepository extends JpaRepository<NotaFiscal, Long>{

	Optional<NotaFiscal> findByChaveNFEAndStatusNot(String chaveNFE, StatusNotaFiscal cancelada);

}
