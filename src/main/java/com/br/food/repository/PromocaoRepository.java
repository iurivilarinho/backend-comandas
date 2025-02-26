package com.br.food.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.br.food.models.Promocao;

@Repository
public interface PromocaoRepository extends JpaRepository<Promocao, Long> {

}
