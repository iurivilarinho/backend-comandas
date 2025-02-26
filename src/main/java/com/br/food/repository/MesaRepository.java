package com.br.food.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.br.food.models.Mesa;

@Repository
public interface MesaRepository extends JpaRepository<Mesa, Long> {

	Optional<Mesa> findByNumero(String numero);

	Mesa findTopByOrderByNumeroDesc();

}
