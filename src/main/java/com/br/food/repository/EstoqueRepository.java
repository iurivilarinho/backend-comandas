package com.br.food.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.br.food.models.Estoque;

@Repository
public interface EstoqueRepository extends JpaRepository<Estoque, Long> {

}
